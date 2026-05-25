document.addEventListener('DOMContentLoaded', function () {
    ocultarSpinner();
    inicializarGrafica();
    inicializarTabla();
    inicializarBusquedaTiempoReal();
});

/* ── Gráfica de rentabilidades ── */
function inicializarGrafica() {
    const canvas = document.getElementById('graficaFic');
    if (!canvas) return;

    // Lee los datos inyectados desde Django
    const labels  = JSON.parse(canvas.dataset.labels  || '[]');
    const valores  = JSON.parse(canvas.dataset.valores || '[]');
    const labelEje = canvas.dataset.label || 'Rentabilidad Anual (%)';

    const colores = valores.map(v =>
        v >= 0 ? 'rgba(25,135,84,0.75)' : 'rgba(220,53,69,0.75)'
    );

    new Chart(canvas, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: labelEje,
                data: valores,
                backgroundColor: colores,
                borderRadius: 6,
                borderSkipped: false,
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: ctx => ` ${ctx.parsed.y.toFixed(2)}%`
                    }
                }
            },
            scales: {
                y: {
                    grid: { color: 'rgba(0,0,0,0.05)' },
                    ticks: {
                        callback: val => val.toFixed(1) + '%'
                    }
                },
                x: {
                    grid: { display: false },
                    ticks: { maxRotation: 35, font: { size: 11 } }
                }
            }
        }
    });
}

/* ── Resalta filas con rentabilidad negativa ── */
function inicializarTabla() {
    document.querySelectorAll('.fic-table tbody tr').forEach(fila => {
        const badges = fila.querySelectorAll('.badge-rent--neg');
        if (badges.length >= 2) {
            fila.style.opacity = '0.85';
        }
    });
}

/* ── Selector de idioma ── */
function toggleLangMenu() {
    const menu  = document.getElementById('langMenu');
    const arrow = document.querySelector('.fic-lang-selector__arrow');

    menu.classList.toggle('open');
    arrow.classList.toggle('open');
}

// Cierra el menú si se hace clic fuera
document.addEventListener('click', function(e) {
    const selector = document.querySelector('.fic-lang-selector');
    if (selector && !selector.contains(e.target)) {
        document.getElementById('langMenu')
            .classList.remove('open');
        document.querySelector('.fic-lang-selector__arrow')
            .classList.remove('open');
    }
});

/* ── Búsqueda en tiempo real ── */
function inicializarBusquedaTiempoReal() {
    const input    = document.getElementById('inputBusqueda');
    const contador = document.getElementById('contadorResultados');

    if (!input) return;

    let timeoutId;

    input.addEventListener('input', function () {
        clearTimeout(timeoutId);

        // Pequeño delay para no filtrar en cada tecla
        timeoutId = setTimeout(() => {
            filtrarTabla(input.value.trim(), contador);
        }, 200);
    });

    // Ejecuta al cargar si ya hay texto
    if (input.value.trim()) {
        filtrarTabla(input.value.trim(), contador);
    }
}

function filtrarTabla(termino, contador) {
    const filas      = document.querySelectorAll('.fic-table tbody tr');
    const terminoMin = termino.toLowerCase();
    let   visibles   = 0;

    filas.forEach(fila => {
        // Busca en nombre del fondo y gestora
        const nombre  = fila.cells[0]
            ? fila.cells[0].textContent.toLowerCase() : '';
        const gestora = fila.cells[1]
            ? fila.cells[1].textContent.toLowerCase() : '';

        const coincide = terminoMin === ''
            || nombre.includes(terminoMin)
            || gestora.includes(terminoMin);

        if (coincide) {
            fila.classList.remove('oculto');
            visibles++;

            // Resalta el texto encontrado
            if (terminoMin !== '') {
                resaltarTexto(fila.cells[0], termino);
                resaltarTexto(fila.cells[1], termino);
            } else {
                limpiarResaltado(fila.cells[0]);
                limpiarResaltado(fila.cells[1]);
            }
        } else {
            fila.classList.add('oculto');
            limpiarResaltado(fila.cells[0]);
            limpiarResaltado(fila.cells[1]);
        }
    });

    // Actualiza el contador
    if (contador) {
        if (terminoMin === '') {
            contador.innerHTML = '';
        } else {
            contador.innerHTML = `
                Mostrando <strong>${visibles}</strong>
                de <strong>${filas.length}</strong> fondos
                para "<strong>${termino}</strong>"
            `;
        }
    }
}

function resaltarTexto(celda, termino) {
    if (!celda) return;

    // Limpia resaltado previo
    limpiarResaltado(celda);

    const texto = celda.textContent;
    const regex = new RegExp(`(${escapeRegex(termino)})`, 'gi');

    if (regex.test(texto)) {
        celda.innerHTML = texto.replace(
            new RegExp(`(${escapeRegex(termino)})`, 'gi'),
            '<span class="fic-highlight">$1</span>'
        );
    }
}

function limpiarResaltado(celda) {
    if (!celda) return;
    const spans = celda.querySelectorAll('.fic-highlight');
    spans.forEach(span => {
        span.replaceWith(span.textContent);
    });
}

function escapeRegex(texto) {
    return texto.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

/* ── Panel lateral KPI ── */
function abrirPanel(tipo) {
    const panel  = document.getElementById('kpiPanel');
    const titulo = document.getElementById('panelTitulo');
    const cuerpo = document.getElementById('panelCuerpo');

    const stats = (typeof FIC_STATS !== 'undefined') ? FIC_STATS : {};
    console.log('Stats cargados:', stats);

    let tituloTexto = '';
    let contenido   = '';

    switch (tipo) {

        case 'fondos':
            tituloTexto = '📊 Total de Fondos';
            contenido = `
                <div class="panel-stat">
                    <div class="panel-stat__icono">📊</div>
                    <div class="panel-stat__info">
                        <div class="panel-stat__valor">${stats.totalFondos}</div>
                        <div class="panel-stat__label">Fondos registrados</div>
                    </div>
                </div>
                <hr class="panel-divider">
                <p class="panel-titulo-seccion">¿Qué significa?</p>
                <p style="font-size:0.9rem;color:#444;line-height:1.6">
                    Este número representa el total de Fondos de Inversión 
                    Colectiva (FIC) registrados en la plataforma, obtenidos 
                    directamente desde datos.gov.co.
                </p>
                <hr class="panel-divider">
                <p class="panel-titulo-seccion">Fuente</p>
                <p style="font-size:0.85rem;color:#666">
                    Superintendencia Financiera de Colombia · datos.gov.co
                </p>
            `;
            break;

        case 'mejor':
            const colorMejor = stats.mejorRentabilidad >= 0
                ? '#065f46' : '#991b1b';
            tituloTexto = '🏆 Mejor Rentabilidad';
            contenido = `
                <div class="panel-stat">
                    <div class="panel-stat__icono">🏆</div>
                    <div class="panel-stat__info">
                        <div class="panel-stat__valor"
                             style="color:${colorMejor}">
                            ${stats.mejorRentabilidad}%
                        </div>
                        <div class="panel-stat__label">Rentabilidad anual</div>
                    </div>
                </div>
                <div class="panel-stat">
                    <div class="panel-stat__icono">🏦</div>
                    <div class="panel-stat__info">
                        <div class="panel-stat__valor"
                             style="font-size:1rem">
                            ${stats.nombreMejorFondo}
                        </div>
                        <div class="panel-stat__label">Nombre del fondo</div>
                    </div>
                </div>
                <hr class="panel-divider">
                <p class="panel-titulo-seccion">¿Qué significa?</p>
                <p style="font-size:0.9rem;color:#444;line-height:1.6">
                    La rentabilidad anual indica el porcentaje de ganancia 
                    o pérdida que generó el fondo en los últimos 12 meses 
                    sobre el capital invertido.
                </p>
            `;
            break;

        case 'promedio':
            const colorProm = stats.promedioRentabilidad >= 0
                ? '#065f46' : '#991b1b';
            tituloTexto = '📈 Rentabilidad Promedio';
            contenido = `
                <div class="panel-stat">
                    <div class="panel-stat__icono">📈</div>
                    <div class="panel-stat__info">
                        <div class="panel-stat__valor"
                             style="color:${colorProm}">
                            ${stats.promedioRentabilidad}%
                        </div>
                        <div class="panel-stat__label">Promedio anual</div>
                    </div>
                </div>
                <hr class="panel-divider">
                <p class="panel-titulo-seccion">¿Qué significa?</p>
                <p style="font-size:0.9rem;color:#444;line-height:1.6">
                    Es el promedio de la rentabilidad anual de todos los fondos 
                    registrados. Un valor positivo indica que en promedio los 
                    fondos están generando ganancias para los inversionistas.
                </p>
                <hr class="panel-divider">
                <p class="panel-titulo-seccion">Interpretación</p>
                <div class="panel-stat" style="background:${
                    stats.promedioRentabilidad >= 0
                        ? '#d1fae5' : '#fee2e2'}">
                    <div class="panel-stat__icono">
                        ${stats.promedioRentabilidad >= 0 ? '✅' : '⚠️'}
                    </div>
                    <div class="panel-stat__info">
                        <div class="panel-stat__valor" style="font-size:0.95rem">
                            ${stats.promedioRentabilidad >= 0
                                ? 'Mercado con tendencia positiva'
                                : 'Mercado con tendencia negativa'}
                        </div>
                    </div>
                </div>
            `;
            break;

        case 'inversionistas':
            tituloTexto = '👥 Total Inversionistas';
            contenido = `
                <div class="panel-stat">
                    <div class="panel-stat__icono">👥</div>
                    <div class="panel-stat__info">
                        <div class="panel-stat__valor">
                            ${stats.totalInversionistas.toLocaleString()}
                        </div>
                        <div class="panel-stat__label">
                            Inversionistas activos
                        </div>
                    </div>
                </div>
                <hr class="panel-divider">
                <p class="panel-titulo-seccion">¿Qué significa?</p>
                <p style="font-size:0.9rem;color:#444;line-height:1.6">
                    Representa el número total de inversionistas 
                    participando en todos los fondos registrados en 
                    la plataforma actualmente.
                </p>
                <hr class="panel-divider">
                <p class="panel-titulo-seccion">Promedio por fondo</p>
                <div class="panel-stat">
                    <div class="panel-stat__icono">🧮</div>
                    <div class="panel-stat__info">
                        <div class="panel-stat__valor">
                            ${stats.totalFondos > 0
                                ? Math.round(stats.totalInversionistas
                                    / stats.totalFondos)
                                : 0}
                        </div>
                        <div class="panel-stat__label">
                            Inversionistas por fondo
                        </div>
                    </div>
                </div>
            `;
            break;
    }

    titulo.textContent = tituloTexto;
    cuerpo.innerHTML   = contenido;
    panel.classList.add('open');

    // Cierra con Escape
    document.addEventListener('keydown', cerrarConEscape);
}

function cerrarPanel() {
    document.getElementById('kpiPanel').classList.remove('open');
    document.removeEventListener('keydown', cerrarConEscape);
}

function cerrarConEscape(e) {
    if (e.key === 'Escape') cerrarPanel();
}

/* ── Sincronizar datos ── */
function sincronizarDatos() {
    const btn = document.querySelector('.btn-export--sync');

    // Cambia el botón a estado cargando
    btn.textContent = '⏳ Sincronizando...';
    btn.classList.add('cargando');

    mostrarToast('⏳ Sincronizando datos desde datos.gov.co...', 'loading', 0);

    fetch('https://localhost:8443/api/fic/sync?limit=100', {
        method: 'GET',
    })
    .then(response => response.text())
    .then(resultado => {
        // Restaura el botón
        btn.textContent = '🔄 Sync';
        btn.classList.remove('cargando');

        mostrarToast('✅ ' + resultado, 'success', 4000);

        // Recarga la página después de 1.5 segundos
        setTimeout(() => {
            mostrarSpinner('🔄 Actualizando datos...');
            window.location.reload();
        }, 1500);
    })
    .catch(error => {
        btn.textContent = '🔄 Sync';
        btn.classList.remove('cargando');
        mostrarToast('❌ Error al sincronizar', 'error', 4000);
        console.error('Error sync:', error);
    });
}

/* ── Toast de notificación ── */
function mostrarToast(mensaje, tipo, duracion) {
    // Elimina toast anterior si existe
    const anterior = document.querySelector('.fic-toast');
    if (anterior) anterior.remove();

    const toast = document.createElement('div');
    toast.className = `fic-toast fic-toast--${tipo}`;
    toast.textContent = mensaje;
    document.body.appendChild(toast);

    // Anima entrada
    requestAnimationFrame(() => {
        requestAnimationFrame(() => {
            toast.classList.add('visible');
        });
    });

    // Auto cierra si duracion > 0
    if (duracion > 0) {
        setTimeout(() => {
            toast.classList.remove('visible');
            setTimeout(() => toast.remove(), 300);
        }, duracion);
    }
}

/* ── Spinner ── */
function ocultarSpinner() {
    const spinner = document.getElementById('ficSpinner');
    if (!spinner) return;
    spinner.classList.add('oculto');
    setTimeout(() => spinner.remove(), 300);
}

function mostrarSpinner(texto) {
    const spinner = document.getElementById('ficSpinner');
    if (!spinner) return;
    const txt = spinner.querySelector('.fic-spinner__texto');
    if (txt && texto) txt.textContent = texto;
    spinner.classList.remove('oculto');
}
