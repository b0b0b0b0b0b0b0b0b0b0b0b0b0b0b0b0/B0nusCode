package com.bobobo.plugins.b0nuscode.ut;

public class TimeFormatter {

    public static String formatMinutes(long minutes) {

        if (minutes >= 525600) {
            long years = minutes / 525600;
            if (years >= 100) {
                long centuries = years / 100;
                return centuries + " " + decline(centuries, "век", "века", "веков");
            }
            return years + " " + decline(years, "год", "года", "лет");
        }

        if (minutes >= 43800) {
            long months = minutes / 43800;
            return months + " " + decline(months, "месяц", "месяца", "месяцев");
        }

        if (minutes >= 10080) {
            long weeks = minutes / 10080;
            return weeks + " " + decline(weeks, "неделю", "недели", "недель");
        }

        if (minutes >= 1440) {
            long days = minutes / 1440;
            return days + " " + decline(days, "день", "дня", "дней");
        }

        if (minutes >= 60) {
            long hours = minutes / 60;
            return hours + " " + decline(hours, "час", "часа", "часов");
        }

        return minutes + " " + decline(minutes, "минуту", "минуты", "минут");
    }

    public static String decline(long number, String one, String few, String many) {
        long n = Math.abs(number) % 100;
        long n1 = n % 10;

        if (n > 10 && n < 20) {
            return many;
        }
        if (n1 > 1 && n1 < 5) {
            return few;
        }
        if (n1 == 1) {
            return one;
        }
        return many;
    }

    public static String formatMinutesDetailed(long minutes) {
        StringBuilder result = new StringBuilder();

        if (minutes >= 525600) {
            long years = minutes / 525600;
            if (years >= 100) {
                long centuries = years / 100;
                result.append(centuries).append(" ").append(decline(centuries, "век", "века", "веков"));
                years = years % 100;
                if (years > 0) {
                    result.append(" ").append(years).append(" ").append(decline(years, "год", "года", "лет"));
                }
            } else {
                result.append(years).append(" ").append(decline(years, "год", "года", "лет"));
            }
            minutes = minutes % 525600;
            if (minutes > 0 && minutes < 43800) {
                return result.toString();
            }
        }

        if (minutes >= 43800) {
            long months = minutes / 43800;
            if (!result.isEmpty()) result.append(" ");
            result.append(months).append(" ").append(decline(months, "месяц", "месяца", "месяцев"));
            minutes = minutes % 43800;
            if (minutes > 0 && minutes < 10080) {
                return result.toString();
            }
        }

        if (minutes >= 10080) {
            long weeks = minutes / 10080;
            if (!result.isEmpty()) result.append(" ");
            result.append(weeks).append(" ").append(decline(weeks, "неделю", "недели", "недель"));
            minutes = minutes % 10080;
            if (minutes > 0 && minutes < 1440) {
                return result.toString();
            }
        }

        if (minutes >= 1440) {
            long days = minutes / 1440;
            if (!result.isEmpty()) result.append(" ");
            result.append(days).append(" ").append(decline(days, "день", "дня", "дней"));
            minutes = minutes % 1440;
            if (minutes > 0 && minutes < 60) {
                return result.toString();
            }
        }

        if (minutes >= 60) {
            long hours = minutes / 60;
            if (!result.isEmpty()) result.append(" ");
            result.append(hours).append(" ").append(decline(hours, "час", "часа", "часов"));
            minutes = minutes % 60;
        }

        if (minutes > 0) {
            if (!result.isEmpty()) result.append(" ");
            result.append(minutes).append(" ").append(decline(minutes, "минуту", "минуты", "минут"));
        }

        return result.toString();
    }
}
