package br.ifsp.vvts.suites;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages("br.ifsp.vvts")
@IncludeTags("Functional")
public class FunctionalTestSuite {
}