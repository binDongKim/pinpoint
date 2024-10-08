/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.navercorp.pinpoint.web.servlet;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

public class VersionPrefixRewriter {
    static final String DEFAULT_MAIN_PATH = "/index.html";
    static final List<String> DEFAULT_RESOURCE_PATHS = List.of("/assets", "/fonts", "/img");
    private static final String API_PREFIX = "api";

    private final VersionToken versionToken = new DefaultVersionToken();
    private final String page;

    private final List<String> specialPaths;
    private final List<String> resourcePaths;


    public VersionPrefixRewriter() {
        this(DEFAULT_MAIN_PATH, Collections.emptyList(), DEFAULT_RESOURCE_PATHS);
    }

    public VersionPrefixRewriter(String page, List<String> specialPaths, List<String> resourcePaths) {
        this.page = Objects.requireNonNull(page, "page");
        this.specialPaths = Objects.requireNonNull(specialPaths, "specialPaths");
        this.resourcePaths = Objects.requireNonNull(resourcePaths, "resourcePaths");
    }

    public String rewrite(String uri) {

        final StringTokenizer tokenizer = new StringTokenizer(uri, "/");
        if (!tokenizer.hasMoreTokens()) {
            return page;
        }

        String command = tokenizer.nextToken();
        if (isApiOrSpecialPaths(command)) {
            return null;
        }
        // next token
        String version = null;
        if (versionToken.isVersion(command)) {
            version = command;
            command = nextToken(tokenizer);
        }
        if (command != null) {
            // static resource
            if (isStaticResource(command)) {
                return null;
            }
        }

        if (version == null) {
            return page;
        } else {
            return '/' + version + page;
        }
    }

    private String nextToken(StringTokenizer tokenizer) {
        if (tokenizer.hasMoreTokens()) {
            return tokenizer.nextToken();
        }
        return null;
    }

    private boolean isApiOrSpecialPaths(String command) {
        if (command.startsWith(API_PREFIX)) {
            return true;
        }
        if (specialPaths.contains("/" + command)) {
            return true;
        }
        return false;
    }

    private boolean isStaticResource(String command) {
        if (resourcePaths.contains("/" + command)) {
            return true;
        }
        return command.indexOf('.') != -1;
    }

}
