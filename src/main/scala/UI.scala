final case class UI(analysisModule: AnalysisModule) {
    
  def consoleLoop(): Unit = {
    var input = ""
    while (input != "exit") {
      input = scala.io.StdIn.readLine()
      println(input)
    }
  }

  def statsChoiceMenu(): Unit = {
    
  }
}
