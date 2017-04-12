package com.tle.web.qti.viewer.questions.renderer.interaction.unsupported;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.ExtendedTextInteraction;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.NonNullByDefault;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.qti.viewer.questions.renderer.base.BlockInteractionRenderer;
import com.tle.web.qti.viewer.questions.renderer.unsupported.UnsupportedQuestionException;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author Aaron
 */
@NonNullByDefault
public class ExtendedTextInteractionRenderer extends BlockInteractionRenderer
{
	// private ExtendedTextInteraction model;

	@AssistedInject
	protected ExtendedTextInteractionRenderer(@Assisted ExtendedTextInteraction model,
		@Assisted QtiViewerContext context)
	{
		super(model, context);
		// this.model = model;
	}

	@Override
	protected SectionRenderable createTopRenderable()
	{
		throw new UnsupportedQuestionException("extendedTextInteraction");
	}

	// @NonNull
	// @Override
	// protected SectionRenderable createTopRenderable()
	// {
	// SectionRenderable superRenderable = super.createTopRenderable();
	// HtmlTextFieldState textBox = new HtmlTextFieldState();
	// textBox.setId(id(model.getId()));
	// // FIXME: state
	// // textBox.setValue(model.getValue());
	// Integer expectedLines = model.getExpectedLines();
	// if( expectedLines != null )
	// {
	// textBox.setStyle("height: " + Integer.toString(expectedLines) +
	// "em");
	// }
	// return CombinedRenderer.combineMultipleResults(superRenderable, new
	// TextAreaRenderer(textBox));
	// }
}
