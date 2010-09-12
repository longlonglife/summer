/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.asual.summer.core.spring;

import java.net.URL;
import java.util.Locale;

import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

public class ExtendedInternalResourceViewResolver extends InternalResourceViewResolver {
    
    public ExtendedInternalResourceViewResolver() {
        setContentType("text/html;charset=UTF-8");
    }

	public String getPrefix() {
		return super.getPrefix();
	}

	public String getSuffix() {
		return super.getSuffix();
	}

	public View resolveViewName(String viewName, Locale locale) throws Exception {
		if (!viewName.startsWith("/")) {
			viewName = "/" + viewName;
		}
		if (viewName.endsWith(getSuffix())) {
			viewName = viewName.substring(0, viewName.length() - getSuffix().length());
		}
		String path = getPrefix() + viewName + getSuffix();
		URL url = getClass().getClassLoader().getResource(path);
		if (url != null) {
			return super.resolveViewName(viewName, locale);
		}
		return null;
	}
	
}