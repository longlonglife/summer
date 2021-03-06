/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.asual.summer.core.faces;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.ListenerFor;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.render.Renderer;

import org.jboss.el.lang.EvaluationContext;
import org.jboss.el.lang.ExpressionBuilder;

import com.asual.summer.core.util.RequestUtils;
import com.asual.summer.core.util.ScriptUtils;
import com.asual.summer.core.util.StringUtils;
import com.sun.faces.facelets.compiler.UIInstructions;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@ListenerFor(systemEventClass=PostAddToViewEvent.class)
public class OutputComponentRenderer extends Renderer implements ComponentSystemEventListener {
	
	private static final String COMP_KEY = OutputComponentRenderer.class.getName() + ".COMPOSITE_COMPONENT";
	
	private static final List<String> ATTRIBUTES = Arrays.asList(new String[] {
		"charset",
		"content",
		"href",
		"hreflang",
		"http-equiv",
		"media",
		"name",
		"rel",
		"sizes",
		"src",
		"type"
	});

	public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
		UIComponent component = event.getComponent();
		FacesContext context = FacesContext.getCurrentInstance();
		// TODO: Implement target for the base tag
		String target = verifyTarget((String) component.getAttributes().get("target"));
		if (target != null) {
			UIComponent cc = UIComponent.getCurrentCompositeComponent(context);
			if (cc != null) {
				component.getAttributes().put(COMP_KEY, cc.getClientId(context));
			}
			context.getViewRoot().addComponentResource(context, component, target);
		}
	}

	public void encodeBegin(FacesContext context, UIComponent component)
		  throws IOException {

		String ccID = (String) component.getAttributes().get(COMP_KEY);
		UIComponent cc = context.getViewRoot().findComponent(':' + ccID);
		UIComponent curCC = UIComponent.getCurrentCompositeComponent(context);
		if (cc != curCC) {
			component.popComponentFromEL(context);
			component.pushComponentToEL(context, cc);
			component.pushComponentToEL(context, component);
		}
		
		if (!isScriptExpression(component)) {
			
			Map<String, Object> attrs = component.getAttributes();
			String qName = (String) attrs.get(FacesDecorator.QNAME);
			
			ResponseWriter writer = context.getResponseWriter();
			writer.startElement(qName, component);
			
			for (String attr : ATTRIBUTES) {
				String value = (String) attrs.get(attr);
				if (value == null && component.getValueExpression(attr) != null) {
					value = (String) component.getValueExpression(attr).getValue(context.getELContext());
				}
				if (value != null) {
					if ("href".equals(attr) || "src".equals(attr)) {
						writer.writeAttribute(attr, RequestUtils.contextRelative((String) attrs.get(attr), true), attr);					
					} else {
						writer.writeAttribute(attr, attrs.get(attr), attr);
					}
				}
			}
		}
	}
	
	public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
		if (!isScriptExpression(component)) {
			Map<String, Object> attrs = component.getAttributes();
			String qName = (String) attrs.get(FacesDecorator.QNAME);
			String escape = ComponentUtils.getAttrValue((Component) component, "dataEscape");
			if (((ComponentUtils.isStyleOrScript(qName) && (StringUtils.isEmpty(escape) || !Boolean.valueOf(escape))) || 
					(!ComponentUtils.isStyleOrScript(qName) && !StringUtils.isEmpty(escape) && !Boolean.valueOf(escape))) &&
					component.getChildCount() == 1 && component.getChildren().get(0) instanceof UIInstructions) {
				context.getResponseWriter().write(ComponentUtils.getValue(component));
			} else {
	            for (UIComponent kid : component.getChildren()) {
	                kid.encodeAll(context);
	            }
			}
		} else {
			List<UIComponent> children = component.getChildren();
			ScriptUtils.define(children.get(0).toString().replaceAll("\\\n|\\\t", "").trim());
		}
	}
    
	public void encodeEnd(FacesContext context, UIComponent component)
		  throws IOException {
		
		String ccID = (String) component.getAttributes().get(COMP_KEY);
		if (ccID != null) {
			component.popComponentFromEL(context);
			component.popComponentFromEL(context);
			component.pushComponentToEL(context, component);
		}
		
		if (!isScriptExpression(component)) {
			ResponseWriter writer = context.getResponseWriter();
			Map<String,Object> attrs = component.getAttributes();		
			String qName = (String) attrs.get(FacesDecorator.QNAME);
			writer.endElement(qName);
		}
	}
	
	protected String verifyTarget(String toVerify) {
		return toVerify;
	}
	
	protected boolean isScriptExpression(UIComponent component) {
		return "text/expression".equalsIgnoreCase((String) component.getAttributes().get("type"));
	}
	
	public void decode(FacesContext context, UIComponent component) {
	}
	
	protected Object evaluateExpression(String value) {
		return ExpressionBuilder.createNode(value)
			.getValue(new EvaluationContext(FacesContext.getCurrentInstance().getELContext(), null, null));
	}
	
	public boolean getRendersChildren() {
		return true;
	}
}