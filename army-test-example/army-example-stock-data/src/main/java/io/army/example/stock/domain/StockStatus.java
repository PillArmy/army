package io.army.example.stock.domain;

/// Enumeration of **stock market statuses** indicating the current trading state.
///
/// | Constant   | Description                        |
/// |------------|------------------------------------|
/// | `NORMAL`   | Actively traded on the exchange    |
/// | `DELISTED` | Removed from the exchange listing  |
/// | `STOPT`    | Long-term trading suspension       |
/// | `UNKNOWN`  | Status not yet determined          |
public enum StockStatus {

    NORMAL,

    // 退市
    DELISTED,

    // 长期停盘
    STOPT,

    UNKNOWN

}
