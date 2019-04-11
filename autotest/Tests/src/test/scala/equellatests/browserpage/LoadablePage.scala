package equellatests.browserpage

trait LoadablePage extends WaitingBrowserPage {

  def load(): this.type

  def error: Option[ErrorPage] = Some(ErrorPage(ctx)).filter(_.exists)
}
