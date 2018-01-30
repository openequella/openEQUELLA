const SwaggerUI = require('swagger-ui');

SwaggerUI({
  dom_id: '#swagger-ui',
  url: $("base").attr('href') + 'api/swagger.yaml',
})
