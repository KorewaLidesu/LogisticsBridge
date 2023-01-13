package testbridge.network;

import appeng.api.util.AEPartLocation;

public enum GuiIDs {
  NONE, // By default we won't interact with this
  RESULT_PIPE,
  TEMPLATE_PKG,
  SATELLITE_BUS(100, AEPartLocation.values().length);

  final int start;
  final int additional;

  GuiIDs() {
    this.start = ordinal();
    this.additional = 0;
  }

  GuiIDs(int start, int additional) {
    this.start = start;
    this.additional = additional;
  }

  public int begin() {
    return this.start;
  }

  public int end() {
    return this.start + this.additional;
  }

}
