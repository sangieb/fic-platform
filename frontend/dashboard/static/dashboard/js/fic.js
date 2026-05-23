/* ═══════════════════════════════════════
   FIC Colombia — JavaScript principal
   ═══════════════════════════════════════ */

document.addEventListener('DOMContentLoaded', function () {
    inicializarGrafica();
    inicializarTabla();
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