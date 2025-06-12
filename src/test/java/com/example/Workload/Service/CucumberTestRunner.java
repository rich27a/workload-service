package com.example.Workload.Service;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME,
        value = "pretty,html:target/cucumber-reports,json:target/cucumber-reports/Cucumber.json")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME,
        value = "com.example.Workload.Service.component.stepdefinitions")
@ConfigurationParameter(key = Constants.FEATURES_PROPERTY_NAME,
        value = "src/test/resources/features")
public class CucumberTestRunner {
}
