/*
 * The MIT License
 *
 * Copyright (c) 2014, Steve Wagner
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.plugins.SemanticVersioning;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.SemanticVersioning.parsing.BuildDefinitionParser;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;

public class SemanticVersioningBuilder extends Builder {

    private BuildDefinitionParser parser;
    private boolean useJenkinsBuildNumber;

    @DataBoundConstructor
    public SemanticVersioningBuilder(String parser, boolean useJenkinsBuildNumber) {
        this.useJenkinsBuildNumber = useJenkinsBuildNumber;
        try {
            this.parser = (BuildDefinitionParser) Jenkins.getInstance().getExtensionList(parser).iterator().next();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Extension
    public static final SemanticVersioningBuilderDescriptor descriptor = new SemanticVersioningBuilderDescriptor();

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    public boolean getUseJenkinsBuildNumber() {
        return this.useJenkinsBuildNumber;
    }

    /**
     * Used from <tt>config.jelly</tt>.
     *
     * @return the canonical class name of the parser  which the semantic version use
     * to parse version number
     */
    public String getParser() {
        return this.parser.getClass().getCanonicalName();
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        SemanticVersioningProcesser semanticVersioningApp = new SemanticVersioningProcesser(
                build,
                this.parser,
                this.useJenkinsBuildNumber,
                Messages.SEMANTIC_VERSION_FILENAME);
        semanticVersioningApp.determineSemanticVersion();
        return true;
    }

    @Extension(ordinal = 9999)
    public static final class SemanticVersioningBuilderDescriptor extends BuildStepDescriptor<Builder> {
        /**
         * In order to load the persisted global configuration, you have to call
         * load() in the constructor.
         */
        public SemanticVersioningBuilderDescriptor() {
            super(SemanticVersioningBuilder.class);
            load();
        }

        /**
         * Generates ListBoxModel for available BuildDefinitionParsers
         *
         * @return available BuildDefinitionParsers as ListBoxModel
         */
        public ListBoxModel doFillParserItems() {
            ListBoxModel parsersModel = new ListBoxModel();
            for (BuildDefinitionParser parser : Jenkins.getInstance()
                    .getExtensionList(BuildDefinitionParser.class)) {
                parsersModel.add(parser.getDescriptor().getDisplayName(), parser
                        .getClass().getCanonicalName());
            }

            return parsersModel;
        }

        public boolean getDefaultUseJenkinsBuildNumber() {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.DISPLAY_NAME;
        }

        @Override
        public boolean isApplicable(Class clazz) {
            return true;
        }

        @Override
        public Builder newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return super.newInstance(req, formData);
        }
    }
}
