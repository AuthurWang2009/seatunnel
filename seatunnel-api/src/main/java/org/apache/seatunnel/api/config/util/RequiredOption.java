/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.seatunnel.api.config.util;

import org.apache.seatunnel.api.config.ConfigEntry;

import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public interface RequiredOption {

    List<ConfigEntry<?>> getOptions();

    /** These options are mutually exclusive, allowing only one set of options to be configured. */
    @Getter
    class ExclusiveRequiredOptions implements RequiredOption {
        private final List<ConfigEntry<?>> exclusiveOptions;

        public ExclusiveRequiredOptions(@NonNull List<ConfigEntry<?>> exclusiveOptions) {
            this.exclusiveOptions = exclusiveOptions;
        }

        public static ExclusiveRequiredOptions of(ConfigEntry<?>... options) {
            return new ExclusiveRequiredOptions(new ArrayList<>(Arrays.asList(options)));
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ExclusiveRequiredOptions)) {
                return false;
            }
            ExclusiveRequiredOptions that = (ExclusiveRequiredOptions) obj;
            return Objects.equals(this.exclusiveOptions, that.exclusiveOptions);
        }

        @Override
        public int hashCode() {
            return Objects.hash(exclusiveOptions);
        }

        @Override
        public String toString() {
            return String.format(
                    "Exclusive required set options: %s",
                    OptionUtil.getOptionKeys(exclusiveOptions));
        }

        @Override
        public List<ConfigEntry<?>> getOptions() {
            return exclusiveOptions;
        }
    }

    /** The option is required. */
    class AbsolutelyRequiredOptions implements RequiredOption {
        @Getter private final List<ConfigEntry<?>> requiredOption;

        AbsolutelyRequiredOptions(List<ConfigEntry<?>> requiredOption) {
            this.requiredOption = requiredOption;
        }

        public static AbsolutelyRequiredOptions of(ConfigEntry<?>... requiredOption) {
            return new AbsolutelyRequiredOptions(new ArrayList<>(Arrays.asList(requiredOption)));
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AbsolutelyRequiredOptions)) {
                return false;
            }
            AbsolutelyRequiredOptions that = (AbsolutelyRequiredOptions) obj;
            return Objects.equals(this.requiredOption, that.requiredOption);
        }

        @Override
        public int hashCode() {
            return this.requiredOption.hashCode();
        }

        @Override
        public String toString() {
            return String.format(
                    "Absolutely required options: '%s'", OptionUtil.getOptionKeys(requiredOption));
        }

        @Override
        public List<ConfigEntry<?>> getOptions() {
            return requiredOption;
        }
    }

    class ConditionalRequiredOptions implements RequiredOption {
        private final Expression expression;
        private final List<ConfigEntry<?>> requiredOption;

        ConditionalRequiredOptions(Expression expression, List<ConfigEntry<?>> requiredOption) {
            this.expression = expression;
            this.requiredOption = requiredOption;
        }

        public static ConditionalRequiredOptions of(
                Expression expression, List<ConfigEntry<?>> requiredOption) {
            return new ConditionalRequiredOptions(expression, requiredOption);
        }

        public static ConditionalRequiredOptions of(
                Condition<?> condition, List<ConfigEntry<?>> requiredOption) {
            return new ConditionalRequiredOptions(Expression.of(condition), requiredOption);
        }

        public Expression getExpression() {
            return expression;
        }

        public List<ConfigEntry<?>> getRequiredOption() {
            return requiredOption;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ConditionalRequiredOptions)) {
                return false;
            }
            ConditionalRequiredOptions that = (ConditionalRequiredOptions) obj;
            return Objects.equals(this.expression, that.expression)
                    && Objects.equals(this.requiredOption, that.requiredOption);
        }

        @Override
        public int hashCode() {
            return this.requiredOption.hashCode();
        }

        @Override
        public String toString() {
            return String.format(
                    "Condition expression: %s, Required options: %s",
                    expression, OptionUtil.getOptionKeys(requiredOption));
        }

        @Override
        public List<ConfigEntry<?>> getOptions() {
            return requiredOption;
        }
    }

    /** These options are bundled, must be present or absent together. */
    class BundledRequiredOptions implements RequiredOption {
        private final List<ConfigEntry<?>> requiredOption;

        BundledRequiredOptions(List<ConfigEntry<?>> requiredOption) {
            this.requiredOption = requiredOption;
        }

        public static BundledRequiredOptions of(ConfigEntry<?>... requiredOption) {
            return new BundledRequiredOptions(new ArrayList<>(Arrays.asList(requiredOption)));
        }

        public static BundledRequiredOptions of(List<ConfigEntry<?>> requiredOption) {
            return new BundledRequiredOptions(requiredOption);
        }

        public List<ConfigEntry<?>> getRequiredOption() {
            return requiredOption;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof BundledRequiredOptions)) {
                return false;
            }
            BundledRequiredOptions that = (BundledRequiredOptions) obj;
            return Objects.equals(this.requiredOption, that.requiredOption);
        }

        @Override
        public int hashCode() {
            return this.requiredOption.hashCode();
        }

        @Override
        public String toString() {
            return String.format(
                    "Bundled Required options: %s", OptionUtil.getOptionKeys(requiredOption));
        }

        @Override
        public List<ConfigEntry<?>> getOptions() {
            return requiredOption;
        }
    }
}
