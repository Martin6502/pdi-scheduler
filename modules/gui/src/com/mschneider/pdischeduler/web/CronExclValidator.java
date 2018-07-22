/*
 * PDI Scheduler - Scheduler Tool for Pentaho Carte Server
 *
 * Copyright (C) 2018 Martin Schneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.mschneider.pdischeduler.web;

import com.haulmont.cuba.gui.components.Field;
import com.haulmont.cuba.gui.components.ValidationException;
import com.mschneider.pdischeduler.utils.DateExclude;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CronExclValidator implements Field.Validator {

    private static final Logger logger = LoggerFactory.getLogger(CronSpecValidator.class);

    @Override
    public void validate(Object value) throws ValidationException {
        String str = (String) value;

        if (!DateExclude.validateExclStr(str)) {
            logger.debug("exclSpec = " + str + " is invalid");
            throw new ValidationException("Must be a valid exclude specification");
        }

    }

}
