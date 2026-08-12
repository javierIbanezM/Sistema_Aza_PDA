# Recipe: Corregir una Fuga de Memoria por Listeners (JavaScript)

Aplica: [`../knowledge/javascript/dom.md`](../knowledge/javascript/dom.md), [`../knowledge/javascript/memory-management.md`](../knowledge/javascript/memory-management.md), [`../anti-patterns/memory-leak.md`](../anti-patterns/memory-leak.md).

## Pasos

### 1. Confirmar la fuga con dos heap snapshots

Ver [`../knowledge/javascript/debugging.md`](../knowledge/javascript/debugging.md) — nunca "corregir" una sospecha de fuga sin confirmarla primero con evidencia medida (regla general de [`../agents/senior-debugger.md`](../agents/senior-debugger.md)).

### 2. Localizar el listener sin limpieza simétrica

```javascript
// Patrón con fuga: el listener nunca se remueve al desmontar el componente
function montarWidget() {
  const handler = () => actualizar();
  window.addEventListener('resize', handler);
  // sin retorno de función de limpieza
}
```

### 3. Añadir limpieza simétrica explícita

```javascript
function montarWidget() {
  const handler = () => actualizar();
  window.addEventListener('resize', handler);

  return function desmontarWidget() {
    window.removeEventListener('resize', handler); // misma referencia de función exacta
  };
}

// Uso
const desmontar = montarWidget();
// ... más tarde, al destruir el componente ...
desmontar();
```

`removeEventListener` requiere la **misma referencia de función** pasada a `addEventListener` — una función anónima nueva (`() => {...}`) en el `removeEventListener` no coincide con la original y no elimina el listener.

### 4. Para frameworks con ciclo de vida (React/Vue/Angular): usar el hook de limpieza nativo

```javascript
// React
useEffect(() => {
  const handler = () => actualizar();
  window.addEventListener('resize', handler);
  return () => window.removeEventListener('resize', handler); // limpieza automática al desmontar
}, []);
```

### 5. Verificar closures que retienen datos grandes innecesariamente

```javascript
// Antes: el closure retiene "datosCompletos" aunque el handler solo necesita "id"
function registrar(datosCompletos) {
  boton.addEventListener('click', () => procesar(datosCompletos.id));
}

// Después: extraer solo lo necesario antes de crear el closure
function registrar(datosCompletos) {
  const id = datosCompletos.id; // solo esto queda retenido, no el objeto completo
  boton.addEventListener('click', () => procesar(id));
}
```

### 6. Volver a medir con la misma metodología del paso 1

Confirmar que el tipo de objeto que crecía en los snapshots anteriores ya no lo hace tras repetir la misma acción varias veces.

## Checklist final

- [ ] ¿Todo `addEventListener` tiene su `removeEventListener` simétrico con la misma referencia de función?
- [ ] ¿Los closures retienen solo los datos que realmente necesitan, no objetos completos innecesarios?
- [ ] ¿Se ha confirmado la corrección con heap snapshots antes/después, no solo por inspección del código?
- [ ] Ejecutar [`../checklists/architecture.md`](../checklists/architecture.md).

Ver también: [`../snippets/javascript/event-cleanup.js`](../snippets/javascript/event-cleanup.js), [`../anti-patterns/memory-leak.md`](../anti-patterns/memory-leak.md).
