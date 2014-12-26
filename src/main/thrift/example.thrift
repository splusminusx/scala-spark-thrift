namespace java service.example


/**
 * Ошибка.
 **/
exception Error {
  1:string message
}


/**
 * Пример описания сервиса с использованием Thrift IDL.
 **/
service Example {
  /**
   * Получить состояние.
   **/
  bool getState(1:string id);

  /**
   * Оповестить о состоянии.
   **/
  oneway void notify(1:bool state, 2:string id);

  /**
   * Получить состояние небезопасно.
   **/
  bool getUnsafeState(1:string id) throws (1:Error error);
}
