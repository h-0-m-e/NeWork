package ru.netology.nework.utils

class CounterUtil {

    companion object{

        fun shortingByLetters(count: Long): String {
            val out = when ((count / 10) >= 100 && (count / 10) < 100_000) {
                true -> when ((count % 1000) / 100 < 1 || (count >= 10_000)) {
                    true -> (count / 1000).toString() + "K"
                    false -> (count / 1000).toString() +
                            "." + ((count % 1000) / 100).toString() + "K"
                }

                false -> when ((count / 10) >= 100_000) {
                    true -> when ((count % 1000_000) / 100_000 < 1) {
                        true -> (count / 1000_000).toString() + "M"
                        false -> (count / 1000_000).toString() +
                                "." + ((count % 1000_000) / 100_000).toString() + "M"
                    }
                    false -> "$count"
                }
            }
            return out
        }

        fun countPublishDate(dateTime: String): String {

            val date = dateTime.split("T").first()
            val time = dateTime.split("T").last().take(5)

            return "$date   $time"
        }
    }
}
