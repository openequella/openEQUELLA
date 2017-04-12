package com.tle.core.payment.paypal.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Currency;
import java.util.Enumeration;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.google.common.base.Throwables;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.payment.entity.PaymentGateway;
import com.tle.common.payment.entity.Sale;
import com.tle.common.payment.entity.Sale.PaidStatus;
import com.tle.core.guice.Bind;
import com.tle.core.payment.PaymentGatewayConstants;
import com.tle.core.payment.service.PaymentGatewayService;
import com.tle.core.payment.service.SaleService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class PaypalNotificationServlet extends HttpServlet
{
	private static Log LOGGER = LogFactory.getLog(PaypalNotificationServlet.class);

	@Inject
	private PaymentGatewayService gatewayService;
	@Inject
	private SaleService saleService;

	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		String saleUuid = request.getParameter("invoice");

		PaymentGateway gateway = gatewayService.getEnabledGateway("paypal");

		Enumeration<?> en = request.getParameterNames();

		String txnId = request.getParameter("txn_id");
		final Sale existing = saleService.getByReceipt(txnId);
		if( existing != null && existing.getPaidStatus() == PaidStatus.PAID )
		{
			LOGGER.warn("Sale with txnId " + txnId + " already paid");
			response.sendError(403, "Already paid " + existing.getReceipt());
			return;
		}

		String paymentAmount = request.getParameter("mc_gross");
		String paymentCurrency = request.getParameter("mc_currency");

		Sale sale = saleService.getSale(null, saleUuid);
		if( sale == null )
		{
			LOGGER.warn("Invalid sale id " + saleUuid);
			response.sendError(403, "Invalid sale id");
			return;
		}
		Currency currency = sale.getCurrency();
		if( paymentCurrency.equalsIgnoreCase(currency.getCurrencyCode()) )
		{
			final BigDecimal num = parseNumber(paymentAmount, currency);
			final BigDecimal saleTotal = new BigDecimal(sale.getPrice() + sale.getTax()).movePointLeft(currency
				.getDefaultFractionDigits());
			if( num.compareTo(saleTotal) != 0 )
			{
				LOGGER.warn("Invalid amount " + paymentAmount + " for sale " + saleUuid);
				response.sendError(403, "Invalid amount " + num.toPlainString());
				return;
			}
		}
		else
		{
			LOGGER.warn("Invalid currency " + paymentCurrency + " for sale " + saleUuid);
			response.sendError(403, "Invalid currency, required is " + currency.getCurrencyCode());
			return;
		}

		// TODO: record random conversion data:
		// settle_amount = 145.5
		// settle_currency = USD
		// exchange_rate = 1.5

		// FIXME:
		// If a payment received is pending due to pending_reason =
		// multi_currency, the first IPN
		// received would not have the settle_amount, settle_currency, or
		// exchange_rate.

		StringBuilder str = new StringBuilder("cmd=_notify-validate");
		while( en.hasMoreElements() )
		{
			String paramName = (String) en.nextElement();
			str.append('&');
			str.append(paramName);
			str.append('=');
			str.append(URLEncoder.encode(request.getParameter(paramName), Constants.UTF8));
		}

		boolean sandbox = gateway.getAttribute(PaymentGatewayConstants.SANDBOX_KEY, false);
		URL u = new URL(sandbox ? "https://www.sandbox.paypal.com/cgi-bin/webscr"
			: "https://www.paypal.com/cgi-bin/webscr");

		HttpsURLConnection uc = (HttpsURLConnection) u.openConnection();
		uc.setDoOutput(true);
		uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		uc.setRequestProperty("Host", sandbox ? "www.sandbox.paypal.com" : "www.paypal.com");
		PrintWriter pw = new PrintWriter(uc.getOutputStream());
		pw.println(str);
		pw.close();

		try( BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream())) )
		{
			String res = in.readLine();

			// assign posted variables to local variables
			// String itemName = request.getParameter("item_name");
			// String itemNumber = request.getParameter("item_number");
			String paymentStatus = request.getParameter("payment_status");
			// paymentAmount = request.getParameter("mc_gross");
			// paymentCurrency = request.getParameter("mc_currency");
			txnId = request.getParameter("txn_id");
			// String receiverEmail = request.getParameter("receiver_email");
			// String payerEmail = request.getParameter("payer_email");

			// check notification validation
			if( res != null && res.equals("VERIFIED") )
			{
				if( "Completed".equalsIgnoreCase(paymentStatus) )
				{
					sale = saleService.getSale(null, saleUuid);

					// add attributes to the sale
					final Map<String, String[]> parameterMap = request.getParameterMap();
					final PropBagEx pbag = new PropBagEx();
					for( Map.Entry<String, String[]> entry : parameterMap.entrySet() )
					{
						String[] values = entry.getValue();
						for( String v : values )
						{
							pbag.createNode(entry.getKey(), v);
						}
					}
					sale.setData(pbag.toString());
					sale.setPaymentGatewayUuid(gateway.getUuid());

					saleService.commit(null, sale, txnId);
				}
				else if( "Pending".equalsIgnoreCase(paymentStatus) )
				{
					sale = saleService.getSale(null, saleUuid);

					// TODO: refactor
					// add attributes to the sale
					final Map<String, String[]> parameterMap = request.getParameterMap();
					final PropBagEx pbag = new PropBagEx();
					for( Map.Entry<String, String[]> entry : parameterMap.entrySet() )
					{
						String[] values = entry.getValue();
						for( String v : values )
						{
							pbag.createNode(entry.getKey(), v);
						}
					}
					sale.setData(pbag.toString());

					saleService.setPending(null, sale);
				}

				// FIXME: "Denied"
				//
			}
			else
			{
				LOGGER.error("An error occurred. The request was:\n" + str + "\n and the response was"
					+ (res != null ? (":\n" + res) : " NULL !"));
			}
		}
	}

	private BigDecimal parseNumber(String value, Currency currency)
	{
		final DecimalFormat df = (DecimalFormat) (NumberFormat.getNumberInstance(CurrentLocale.getLocale()));
		df.setParseBigDecimal(true);
		final int defaultFractionDigits = currency.getDefaultFractionDigits();
		if( defaultFractionDigits != -1 )
		{
			df.setMinimumFractionDigits(0);
			df.setMaximumFractionDigits(defaultFractionDigits);
		}
		try
		{
			return (BigDecimal) df.parseObject(value);
		}
		catch( ParseException e )
		{
			throw Throwables.propagate(e);
		}
	}
}
