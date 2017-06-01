/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.qti.viewer.questions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import uk.ac.ed.ph.jqtiplus.node.ForeignElement;
import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.content.InfoControl;
import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
import uk.ac.ed.ph.jqtiplus.node.content.basic.TextRun;
import uk.ac.ed.ph.jqtiplus.node.content.mathml.Math;
import uk.ac.ed.ph.jqtiplus.node.content.variable.FeedbackBlock;
import uk.ac.ed.ph.jqtiplus.node.content.variable.FeedbackInline;
import uk.ac.ed.ph.jqtiplus.node.content.variable.PrintedVariable;
import uk.ac.ed.ph.jqtiplus.node.content.variable.RubricBlock;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.hypertext.A;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.image.Img;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.list.Dd;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.list.Dl;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.list.Dt;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.list.Li;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.list.Ol;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.list.Ul;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.object.Object;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.object.Param;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.presentation.Big;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.presentation.Hr;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.presentation.I;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.presentation.Small;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.presentation.Sub;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.presentation.Sup;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.presentation.Tt;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Caption;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Col;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Table;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Tbody;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Td;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Tfoot;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Th;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Thead;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Tr;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Abbr;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Acronym;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Address;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Blockquote;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Br;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Cite;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Code;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Dfn;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Div;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Em;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.H1;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.H2;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.H3;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.H4;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.H5;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.H6;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Kbd;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.P;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Pre;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Q;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Samp;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Span;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Strong;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Var;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.ModalFeedback;
import uk.ac.ed.ph.jqtiplus.node.item.Stylesheet;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.AssociateInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.CustomInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.DrawingInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.EndAttemptInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ExtendedTextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.GapMatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.GraphicAssociateInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.GraphicOrderInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HotspotInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HottextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.InlineChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.MatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.MediaInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.OrderInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Prompt;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.SelectPointInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.SliderInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.TextEntryInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.UploadInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.GapText;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.InlineChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleAssociableChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleMatchSet;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.content.Gap;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.content.Hottext;
import uk.ac.ed.ph.jqtiplus.node.test.TestFeedback;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.core.guice.Bind;
import com.tle.core.guice.BindFactory;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.qti.viewer.questions.renderer.AssessmentItemRenderer;
import com.tle.web.qti.viewer.questions.renderer.FeedbackBlockRenderer;
import com.tle.web.qti.viewer.questions.renderer.FeedbackInlineRenderer;
import com.tle.web.qti.viewer.questions.renderer.ForeignElementRenderer;
import com.tle.web.qti.viewer.questions.renderer.GapTextRenderer;
import com.tle.web.qti.viewer.questions.renderer.InfoControlRenderer;
import com.tle.web.qti.viewer.questions.renderer.InlineChoiceRenderer;
import com.tle.web.qti.viewer.questions.renderer.ItemBodyRenderer;
import com.tle.web.qti.viewer.questions.renderer.MathRenderer;
import com.tle.web.qti.viewer.questions.renderer.ModalFeedbackRenderer;
import com.tle.web.qti.viewer.questions.renderer.PrintedVariableRenderer;
import com.tle.web.qti.viewer.questions.renderer.PromptRenderer;
import com.tle.web.qti.viewer.questions.renderer.QtiNodeRenderer;
import com.tle.web.qti.viewer.questions.renderer.SimpleChoiceRenderer;
import com.tle.web.qti.viewer.questions.renderer.StylesheetRenderer;
import com.tle.web.qti.viewer.questions.renderer.TestFeedbackRenderer;
import com.tle.web.qti.viewer.questions.renderer.TextRunRenderer;
import com.tle.web.qti.viewer.questions.renderer.interaction.ChoiceInteractionRenderer;
import com.tle.web.qti.viewer.questions.renderer.interaction.InlineChoiceInteractionRenderer;
import com.tle.web.qti.viewer.questions.renderer.interaction.TextEntryInteractionRenderer;
import com.tle.web.qti.viewer.questions.renderer.interaction.unsupported.AssociateInteractionRenderer;
import com.tle.web.qti.viewer.questions.renderer.interaction.unsupported.CustomInteractionRenderer;
import com.tle.web.qti.viewer.questions.renderer.interaction.unsupported.DrawingInteractionRenderer;
import com.tle.web.qti.viewer.questions.renderer.interaction.unsupported.EndAttemptInteractionRenderer;
import com.tle.web.qti.viewer.questions.renderer.interaction.unsupported.ExtendedTextInteractionRenderer;
import com.tle.web.qti.viewer.questions.renderer.interaction.unsupported.GapMatchInteractionRenderer;
import com.tle.web.qti.viewer.questions.renderer.interaction.unsupported.GraphicAssociateInteractionRenderer;
import com.tle.web.qti.viewer.questions.renderer.interaction.unsupported.GraphicOrderInteractionRenderer;
import com.tle.web.qti.viewer.questions.renderer.interaction.unsupported.HotspotInteractionRenderer;
import com.tle.web.qti.viewer.questions.renderer.interaction.unsupported.HottextInteractionRenderer;
import com.tle.web.qti.viewer.questions.renderer.interaction.unsupported.MatchInteractionRenderer;
import com.tle.web.qti.viewer.questions.renderer.interaction.unsupported.MediaInteractionRenderer;
import com.tle.web.qti.viewer.questions.renderer.interaction.unsupported.OrderInteractionRenderer;
import com.tle.web.qti.viewer.questions.renderer.interaction.unsupported.SelectPointInteractionRenderer;
import com.tle.web.qti.viewer.questions.renderer.interaction.unsupported.SliderInteractionRenderer;
import com.tle.web.qti.viewer.questions.renderer.interaction.unsupported.UploadInteractionRenderer;
import com.tle.web.qti.viewer.questions.renderer.unsupported.GapRenderer;
import com.tle.web.qti.viewer.questions.renderer.unsupported.HottextRenderer;
import com.tle.web.qti.viewer.questions.renderer.unsupported.RubricBlockRenderer;
import com.tle.web.qti.viewer.questions.renderer.unsupported.SimpleAssociableChoiceRenderer;
import com.tle.web.qti.viewer.questions.renderer.unsupported.SimpleMatchSetRenderer;
import com.tle.web.qti.viewer.questions.renderer.unsupported.UnrenderableNodeTypeException;
import com.tle.web.qti.viewer.questions.renderer.xhtml.ObjectRenderer;
import com.tle.web.qti.viewer.questions.renderer.xhtml.TableRenderer;
import com.tle.web.qti.viewer.questions.renderer.xhtml.XhtmlElementRenderer;

/**
 * @author Aaron
 */
@NonNullByDefault
@Bind
@Singleton
public class QuestionRenderers
{
	private static final Logger LOGGER = Logger.getLogger(QuestionRenderers.class);

	@Inject
	private Factory qfac;

	private static final Map<Class<?>, Method> facMap = Maps.newHashMap();

	@PostConstruct
	public void setupLookupMap()
	{
		for( Method method : Factory.class.getDeclaredMethods() )
		{
			Class<?>[] parameterTypes = method.getParameterTypes();
			if( parameterTypes.length == 2 )
			{
				Class<?> param = parameterTypes[0];
				facMap.put(param, method);
			}
		}
	}

	public QtiNodeRenderer chooseRenderer(@Nullable java.lang.Object model, QtiViewerContext viewer)
	{
		if( model == null )
		{
			return null;
		}

		final Class<? extends java.lang.Object> modelClass = model.getClass();

		final Method method = facMap.get(modelClass);
		if( method != null )
		{
			LOGGER.trace(modelClass.getName() + "." + method.getName());
			try
			{
				final QtiNodeRenderer renderer = (QtiNodeRenderer) method.invoke(qfac, model, viewer);
				renderer.preProcess();
				return renderer;
			}
			catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException e )
			{
				throw Throwables.propagate(e);
			}
		}
		else if( model instanceof QtiNode )
		{
			throw new UnrenderableNodeTypeException("Unrenderable qti node type " + modelClass.getName());
		}
		// Unrenderable type
		throw new UnrenderableNodeTypeException("Unrenderable type " + modelClass.getName());
	}

	/**
	 * Don't need to use this in general, use chooseRenderer instead unless you
	 * need a specific renderer.
	 * 
	 * @return
	 */
	public Factory getRendererFactory()
	{
		return qfac;
	}

	@BindFactory
	public interface Factory
	{
		AssessmentItemRenderer createAssessmentItemRenderer(AssessmentItem item, QtiViewerContext context);

		ItemBodyRenderer createItemBodyRenderer(ItemBody itemBody, QtiViewerContext context);

		ChoiceInteractionRenderer createChoiceInteractionRenderer(ChoiceInteraction model, QtiViewerContext context);

		SimpleChoiceRenderer createSimpleChoiceRenderer(SimpleChoice choice, QtiViewerContext context);

		SimpleChoiceRenderer createSimpleChoiceRenderer(SimpleChoice choice, QtiViewerContext context, String name,
			boolean multiple);

		ExtendedTextInteractionRenderer createExtendedTextInteractionRenderer(ExtendedTextInteraction interaction,
			QtiViewerContext context);

		FeedbackBlockRenderer c(FeedbackBlock fb, QtiViewerContext context);

		XhtmlElementRenderer c(P p, QtiViewerContext context);

		XhtmlElementRenderer c(H1 p, QtiViewerContext context);

		XhtmlElementRenderer c(H2 p, QtiViewerContext context);

		XhtmlElementRenderer c(H3 p, QtiViewerContext context);

		XhtmlElementRenderer c(H4 p, QtiViewerContext context);

		XhtmlElementRenderer c(H5 p, QtiViewerContext context);

		XhtmlElementRenderer c(H6 p, QtiViewerContext context);

		XhtmlElementRenderer c(Pre p, QtiViewerContext context);

		XhtmlElementRenderer cBrRenderer(Br p, QtiViewerContext context);

		XhtmlElementRenderer cHrRenderer(Hr p, QtiViewerContext context);

		XhtmlElementRenderer cDivRenderer(Div p, QtiViewerContext context);

		XhtmlElementRenderer cAbbrRenderer(Abbr p, QtiViewerContext context);

		XhtmlElementRenderer cCodeRenderer(Code p, QtiViewerContext context);

		XhtmlElementRenderer cSpanRenderer(Span p, QtiViewerContext context);

		XhtmlElementRenderer cBlockquoteRenderer(Blockquote p, QtiViewerContext context);

		XhtmlElementRenderer cStrongRenderer(Strong p, QtiViewerContext context);

		XhtmlElementRenderer cARenderer(A p, QtiViewerContext context);

		XhtmlElementRenderer cImgRenderer(Img p, QtiViewerContext context);

		XhtmlElementRenderer cAcronymRenderer(Acronym p, QtiViewerContext context);

		XhtmlElementRenderer cAddressRenderer(Address p, QtiViewerContext context);

		XhtmlElementRenderer cCiteRenderer(Cite p, QtiViewerContext context);

		XhtmlElementRenderer cDfnRenderer(Dfn p, QtiViewerContext context);

		XhtmlElementRenderer cEmRenderer(Em p, QtiViewerContext context);

		XhtmlElementRenderer cKdbRenderer(Kbd p, QtiViewerContext context);

		XhtmlElementRenderer cQRenderer(Q p, QtiViewerContext context);

		XhtmlElementRenderer cSampRenderer(Samp p, QtiViewerContext context);

		XhtmlElementRenderer cVarRenderer(Var p, QtiViewerContext context);

		XhtmlElementRenderer cDlRenderer(Dl p, QtiViewerContext context);

		XhtmlElementRenderer cDdRenderer(Dd p, QtiViewerContext context);

		XhtmlElementRenderer cDtRenderer(Dt p, QtiViewerContext context);

		XhtmlElementRenderer cUlRenderer(Ul p, QtiViewerContext context);

		XhtmlElementRenderer cLiRenderer(Li p, QtiViewerContext context);

		XhtmlElementRenderer cOlRenderer(Ol p, QtiViewerContext context);

		XhtmlElementRenderer cParamRenderer(Param p, QtiViewerContext context);

		XhtmlElementRenderer cBigRenderer(Big p, QtiViewerContext context);

		XhtmlElementRenderer cIRenderer(I p, QtiViewerContext context);

		XhtmlElementRenderer cSmallRenderer(Small p, QtiViewerContext context);

		XhtmlElementRenderer cSubRenderer(Sub p, QtiViewerContext context);

		XhtmlElementRenderer cSupRenderer(Sup p, QtiViewerContext context);

		XhtmlElementRenderer cTtRenderer(Tt p, QtiViewerContext context);

		XhtmlElementRenderer cCaptionRenderer(Caption p, QtiViewerContext context);

		XhtmlElementRenderer cColRenderer(Col p, QtiViewerContext context);

		TableRenderer c(Table p, QtiViewerContext context);

		XhtmlElementRenderer cTbodyRenderer(Tbody p, QtiViewerContext context);

		XhtmlElementRenderer cTdRenderer(Td p, QtiViewerContext context);

		XhtmlElementRenderer createTfootRenderer(Tfoot p, QtiViewerContext context);

		XhtmlElementRenderer c(Th p, QtiViewerContext context);

		XhtmlElementRenderer c(Thead p, QtiViewerContext context);

		XhtmlElementRenderer c(Tr p, QtiViewerContext context);

		TextRunRenderer c(TextRun p, QtiViewerContext context);

		ForeignElementRenderer c(ForeignElement p, QtiViewerContext context);

		MathRenderer c(Math m, QtiViewerContext context);

		FeedbackInlineRenderer c(FeedbackInline p, QtiViewerContext context);

		TestFeedbackRenderer c(TestFeedback p, QtiViewerContext context);

		MatchInteractionRenderer c(MatchInteraction p, QtiViewerContext context);

		GapMatchInteractionRenderer c(GapMatchInteraction p, QtiViewerContext context);

		PromptRenderer c(Prompt p, QtiViewerContext context);

		AssociateInteractionRenderer c(AssociateInteraction p, QtiViewerContext context);

		UploadInteractionRenderer c(UploadInteraction p, QtiViewerContext context);

		OrderInteractionRenderer c(OrderInteraction p, QtiViewerContext context);

		InfoControlRenderer c(InfoControl p, QtiViewerContext context);

		ObjectRenderer c(Object object, QtiViewerContext context);

		TextEntryInteractionRenderer c(TextEntryInteraction p, QtiViewerContext context);

		SimpleAssociableChoiceRenderer c(SimpleAssociableChoice p, QtiViewerContext context);

		GapTextRenderer c(GapText gapText, QtiViewerContext context);

		GapRenderer c(Gap gap, QtiViewerContext context);

		InlineChoiceInteractionRenderer c(InlineChoiceInteraction inlineChoice, QtiViewerContext context);

		InlineChoiceRenderer c(InlineChoice choice, QtiViewerContext context);

		SimpleMatchSetRenderer c(SimpleMatchSet simpleMatchSet, QtiViewerContext context);

		PrintedVariableRenderer c(PrintedVariable printedVariable, QtiViewerContext context);

		RubricBlockRenderer c(RubricBlock rubricBlock, QtiViewerContext context);

		StylesheetRenderer c(Stylesheet stylesheet, QtiViewerContext context);

		HottextInteractionRenderer c(HottextInteraction hottext, QtiViewerContext context);

		EndAttemptInteractionRenderer c(EndAttemptInteraction p, QtiViewerContext context);

		HottextRenderer c(Hottext p, QtiViewerContext context);

		ModalFeedbackRenderer c(ModalFeedback p, QtiViewerContext context);

		// Unsupported
		SelectPointInteractionRenderer c(SelectPointInteraction spi, QtiViewerContext context);

		GraphicOrderInteractionRenderer c(GraphicOrderInteraction gi, QtiViewerContext context);

		HotspotInteractionRenderer c(HotspotInteraction p, QtiViewerContext context);

		GraphicAssociateInteractionRenderer c(GraphicAssociateInteraction p, QtiViewerContext context);

		CustomInteractionRenderer c(CustomInteraction<?> p, QtiViewerContext context);

		DrawingInteractionRenderer c(DrawingInteraction p, QtiViewerContext context);

		SliderInteractionRenderer c(SliderInteraction p, QtiViewerContext context);

		MediaInteractionRenderer c(MediaInteraction p, QtiViewerContext context);
	}

}
