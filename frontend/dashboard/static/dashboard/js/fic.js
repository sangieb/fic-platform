/* ═══════════════════════════════════════
   FIC Colombia — JavaScript principal
   ═══════════════════════════════════════ */

document.addEventListener('DOMContentLoaded', function () {
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