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

package com.asual.summer.core.spring;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.annotation.ModelAndViewResolver;
import org.springframework.web.servlet.view.AbstractView;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.WebUtils;

import com.asual.summer.core.ResponseViews;
import com.asual.summer.core.util.BeanUtils;
import com.asual.summer.core.util.RequestUtils;
import com.asual.summer.core.view.AbstractResponseView;
import com.asual.summer.core.view.ResponseView;

/**
 * 
 * @author Rostislav Georgiev
 * @author Rostislav Hristov
 *
 */
public class AnnotationModelAndViewResolver implements ModelAndViewResolver {
	
	private final Log logger = LogFactory.getLog(getClass());

	@Inject
	private ViewResolverConfiguration viewResolverConfiguration;
	
	private static final UrlPathHelper urlPathHelper = new UrlPathHelper();
	
	public View resolveView(Method handlerMethod, NativeWebRequest webRequest) throws InstantiationException, IllegalAccessException {

		ResponseViews viewAnn = AnnotationUtils.findAnnotation(handlerMethod, ResponseViews.class);
		
		if (viewAnn != null) {
			
			Class<? extends AbstractResponseView>[] values = viewAnn.value();
			List<AbstractView> views = new ArrayList<AbstractView>();
			boolean explicit = viewAnn.explicit();
			
			if (values.length != 0) {
				for (Class<? extends AbstractResponseView> value : values) {
					views.addAll(BeanUtils.getBeansOfType(value).values());
				}
			}
			
			AbstractResponseView view = (AbstractResponseView) handleViews(views, webRequest);
			if (view != null) {
				return view;
			} else if (explicit) {
				return views.get(0);
			}
		}
		
		return null;
	}

	public AbstractView handleViews(Collection<AbstractView> views, NativeWebRequest request) {

		for (AbstractView view : views) {

			if (viewResolverConfiguration.getFavorPathExtension()) {
				String requestUri = urlPathHelper.getRequestUri((HttpServletRequest) request.getNativeRequest());
				String filename = WebUtils.extractFullFilenameFromUrlPath(requestUri);
				String extension = StringUtils.getFilenameExtension(filename);
				if (StringUtils.hasText(extension) && extension.matches(((ResponseView) view).getExtension())) {
					return view;
				}
			}
			
			if (viewResolverConfiguration.getFavorParameter()) {
				if (request.getParameter(viewResolverConfiguration.getParameterName()) != null) {
					String parameterValue = request.getParameter(viewResolverConfiguration.getParameterName());
					if (StringUtils.hasText(parameterValue) && parameterValue.matches(((ResponseView) view).getExtension())) {
						return view;
					}
				}
			}
			
			if (!viewResolverConfiguration.getIgnoreAcceptHeader()) {
				String acceptHeader = StringUtils.arrayToCommaDelimitedString(request.getHeaderValues("Accept"));
				if (StringUtils.hasText(acceptHeader)) {
					List<MediaType> mediaTypes = viewResolverConfiguration.getMediaTypes(RequestUtils.getRequest());
					if (mediaTypes.contains(MediaType.parseMediaType(view.getContentType()))) {
						return view;
					}
				}
			}
		}
		
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	public ModelAndView resolveModelAndView(Method handlerMethod,
			Class handlerType, Object returnValue,
			ExtendedModelMap implicitModel, NativeWebRequest webRequest) {
		
		try {
			
			View view = resolveView(handlerMethod, webRequest);
			
			if (view != null) {
				if (returnValue instanceof ModelAndView) {
					ModelAndView mav = (ModelAndView) returnValue;
					mav.setView(view);
					return mav;
				} else if (returnValue instanceof ModelMap) {
					return new ModelAndView(view, (ModelMap) returnValue);
				}
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return UNRESOLVED;
		
	}
	
}