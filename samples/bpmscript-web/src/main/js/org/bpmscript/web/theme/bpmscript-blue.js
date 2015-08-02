function BpmScriptBlueTheme() {
  this.bpmscript = delegate(this, new BpmScriptTheme());
}
BpmScriptBlueTheme.prototype = {
  renderBox: function(title, content, style) {
    return <table class={"boxtable " + style}>
        <tr class={"boxheader " + style}>
          <td><b>{title}</b></td>
        </tr>
        <tr class={"boxcontent " + style}>
          <td>
            {content}
          </td>
        </tr>
      </table>;
  }
}
