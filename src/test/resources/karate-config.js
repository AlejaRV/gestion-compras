function fn() {
  // Prioridad: -DbaseUrl (inyectado por el runner) > variable de entorno BASE_URL > localhost
  var baseUrl = karate.properties['baseUrl'] || java.lang.System.getenv('BASE_URL') || 'http://localhost:8080';
  karate.log('Karate apuntando a:', baseUrl);

  karate.configure('connectTimeout', 5000);
  karate.configure('readTimeout', 10000);
  karate.configure('logPrettyRequest', true);
  karate.configure('logPrettyResponse', true);

  return {
    baseUrl: baseUrl
  };
}
