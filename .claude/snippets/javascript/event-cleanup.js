// snippets/javascript/event-cleanup.js
// Patrón de limpieza simétrica de listeners — ver recipes/js-fix-memory-leak.md.

function montarWidget() {
  const handlerResize = () => actualizar();
  const handlerScroll = () => actualizarPosicion();

  window.addEventListener('resize', handlerResize);
  window.addEventListener('scroll', handlerScroll, { passive: true });

  return function desmontarWidget() {
    window.removeEventListener('resize', handlerResize);
    window.removeEventListener('scroll', handlerScroll);
  };
}

// Uso
const desmontar = montarWidget();
// ... al destruir el componente ...
// desmontar();

// Equivalente con AbortController (una única llamada limpia varios listeners a la vez)
function montarWidgetConAbortController() {
  const controller = new AbortController();

  window.addEventListener('resize', () => actualizar(), { signal: controller.signal });
  window.addEventListener('scroll', () => actualizarPosicion(), { signal: controller.signal });

  return () => controller.abort(); // una única llamada remueve TODOS los listeners asociados a esta signal
}
