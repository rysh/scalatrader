package com.rysh.experiment.adapter.bitflyer

case class Execution(
                      id: Double,
                      side: String,
                      price: Double,
                      size: Double,
                      exec_date: String,
                      buy_child_order_acceptance_id: String,
                      sell_child_order_acceptance_id: String
                    )