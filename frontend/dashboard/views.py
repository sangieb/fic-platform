import requests
import urllib3
from django.shortcuts import render
from django.conf import settings

# Suprime advertencias de certificado autofirmado
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

TRADUCCIONES = {
    'es': {
        'dashboard_titulo':             'Fondos de Inversión Colectiva de Colombia',
        'dashboard_buscador':           'Buscar por nombre de fondo...',
        'dashboard_boton_buscar':       'Buscar',
        'dashboard_boton_limpiar':      'Limpiar',
        'fondos_disponibles':           'fondos disponibles · Fuente: datos.gov.co',
        'grafica_titulo':               'Rentabilidad Anual — Top 10 Fondos',
        'tabla_titulo':                 'Listado de Fondos',
        'tabla_columna_fondo':          'Nombre del Fondo',
        'tabla_columna_gestora':        'Gestora',
        'tabla_columna_tipo':           'Tipo',
        'tabla_columna_diaria':         'Diaria',
        'tabla_columna_mensual':        'Mensual',
        'tabla_columna_anual':          'Anual',
        'tabla_columna_inversionistas': 'Inversionistas',
        'tabla_columna_periodo':        'Período',
        'tabla_sin_resultados':         'No se encontraron fondos',
        'paginacion_anterior':          'Anterior',
        'paginacion_siguiente':         'Siguiente',
        'paginacion_pagina':            'Página',
        'nav_exportar_pdf':             'Exportar PDF',
        'nav_exportar_excel':           'Exportar Excel',
        'page_title':                   'FIC Colombia — Fondos de Inversión',
        'kpi_total_fondos':             'Total Fondos',
        'kpi_mejor_rent':               'Mejor Rentabilidad Anual',
        'kpi_promedio':                 'Rentabilidad Promedio',
        'kpi_inversionistas':           'Total Inversionistas',
        'kpi_mejor_fondo':              'Mejor Fondo',
    },
    'en': {
        'dashboard_titulo':             'Collective Investment Funds of Colombia',
        'dashboard_buscador':           'Search by fund name...',
        'dashboard_boton_buscar':       'Search',
        'dashboard_boton_limpiar':      'Clear',
        'fondos_disponibles':           'funds available · Source: datos.gov.co',
        'grafica_titulo':               'Annual Yield — Top 10 Funds',
        'tabla_titulo':                 'Fund List',
        'tabla_columna_fondo':          'Fund Name',
        'tabla_columna_gestora':        'Manager',
        'tabla_columna_tipo':           'Type',
        'tabla_columna_diaria':         'Daily',
        'tabla_columna_mensual':        'Monthly',
        'tabla_columna_anual':          'Annual',
        'tabla_columna_inversionistas': 'Investors',
        'tabla_columna_periodo':        'Period',
        'tabla_sin_resultados':         'No funds found',
        'paginacion_anterior':          'Previous',
        'paginacion_siguiente':         'Next',
        'paginacion_pagina':            'Page',
        'nav_exportar_pdf':             'Export PDF',
        'nav_exportar_excel':           'Export Excel',
        'page_title':                   'FIC Colombia — Investment Funds',
        'kpi_total_fondos':             'Total Funds',
        'kpi_mejor_rent':               'Best Annual Yield',
        'kpi_promedio':                 'Average Yield',
        'kpi_inversionistas':           'Total Investors',
        'kpi_mejor_fondo':              'Best Fund',
    },
    'fr': {
        'dashboard_titulo':             "Fonds d'Investissement Collectif de Colombie",
        'dashboard_buscador':           'Rechercher par nom de fonds...',
        'dashboard_boton_buscar':       'Rechercher',
        'dashboard_boton_limpiar':      'Effacer',
        'fondos_disponibles':           'fonds disponibles · Source: datos.gov.co',
        'grafica_titulo':               'Rentabilité Annuelle — Top 10 Fonds',
        'tabla_titulo':                 'Liste des Fonds',
        'tabla_columna_fondo':          'Nom du Fonds',
        'tabla_columna_gestora':        'Gestionnaire',
        'tabla_columna_tipo':           'Type',
        'tabla_columna_diaria':         'Quotidien',
        'tabla_columna_mensual':        'Mensuel',
        'tabla_columna_anual':          'Annuel',
        'tabla_columna_inversionistas': 'Investisseurs',
        'tabla_columna_periodo':        'Période',
        'tabla_sin_resultados':         'Aucun fonds trouvé',
        'paginacion_anterior':          'Précédent',
        'paginacion_siguiente':         'Suivant',
        'paginacion_pagina':            'Page',
        'nav_exportar_pdf':             'Exporter PDF',
        'nav_exportar_excel':           'Exporter Excel',
        'page_title':                   "FIC Colombie — Fonds d'Investissement",
        'kpi_total_fondos':             'Total Fonds',
        'kpi_mejor_rent':               'Meilleure Rentabilité Annuelle',
        'kpi_promedio':                 'Rentabilité Moyenne',
        'kpi_inversionistas':           'Total Investisseurs',
        'kpi_mejor_fondo':              'Meilleur Fonds',
    },
}

def get_texto(lang, clave):
    idioma = TRADUCCIONES.get(lang, TRADUCCIONES['es'])
    return idioma.get(clave, TRADUCCIONES['es'].get(clave, clave))

def index(request):
    page   = int(request.GET.get('page', 0))
    nombre = request.GET.get('nombre', '')
    lang   = request.GET.get('lang', 'es')

    if lang not in ('es', 'en', 'fr'):
        lang = 'es'

    params = {'page': page, 'size': 100}
    if nombre:
        params['nombre'] = nombre

    # Fondos
    try:
        resp = requests.get(
            f"{settings.BACKEND_URL}/api/fic",
            params=params,
            timeout=10,
            verify=False
        )
        data        = resp.json()
        fondos      = data.get('content', [])
        total_pages = data.get('totalPages', 0)
        total_items = data.get('totalElements', 0)
    except Exception as e:
        print(f"Error fondos: {e}")
        fondos      = []
        total_pages = 0
        total_items = 0

    # Estadísticas KPI
    try:
        resp_stats = requests.get(
            f"{settings.BACKEND_URL}/api/fic/estadisticas",
            timeout=10,
            verify=False
        )
        stats = resp_stats.json()
    except Exception as e:
        print(f"Error stats: {e}")
        stats = {
            'totalFondos':          0,
            'mejorRentabilidad':    0,
            'promedioRentabilidad': 0,
            'totalInversionistas':  0,
            'nombreMejorFondo':     '-'
        }

    t = TRADUCCIONES.get(lang, TRADUCCIONES['es'])

    return render(request, 'dashboard/index.html', {
        'fondos':       fondos,
        'total_pages':  total_pages,
        'total_items':  total_items,
        'current_page': page,
        'nombre':       nombre,
        'lang':         lang,
        't':            t,
        'stats':        stats,
    })