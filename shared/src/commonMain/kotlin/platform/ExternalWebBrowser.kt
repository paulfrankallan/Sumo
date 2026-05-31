package platform

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class ExternalWebBrowser {
    fun launch(url: String)
}
