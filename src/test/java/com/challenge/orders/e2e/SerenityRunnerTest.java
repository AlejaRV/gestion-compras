package com.challenge.orders.e2e;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        features = "src/test/resources/features/orders",
        glue = "com.challenge.orders.e2e.stepdefinitions",
        plugin = {"pretty"}
)
public class SerenityRunnerTest {
}
