package com.tank.util;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Cell {

  private String name = "";

  private int column = 0;

  private int depth = 0;
}
