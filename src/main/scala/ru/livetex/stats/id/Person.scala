package ru.livetex.stats.id


/**
 * Схема хранения персоны.
 * @param id - идентификатор персоны.
 * @param requestCount - количество запросов идентификации.
 */
case class Person(id: String, requestCount: Int)
