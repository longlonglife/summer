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

package com.asual.summer.sample.convert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.asual.summer.sample.domain.Technology.Image;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Component
public class MultipartFileToTechnologyImageConvertor implements Converter<MultipartFile, Image> {

    private final Log logger = LogFactory.getLog(getClass());

    @Override
	public Image convert(MultipartFile source) {
		if (source.getSize() != 0) {
			try {
				return new Image(source);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return null;
	}

}