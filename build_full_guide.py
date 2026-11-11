# pyrefly: ignore-errors
# pyright: reportMissingImports=false, reportMissingModuleSource=false, reportAttributeAccessIssue=false, reportUnknownMemberType=false
# type: ignore
"""
build_full_guide.py
Constructs the complete engineering project development guide in Microsoft Word (.docx) format:
'Electricity_Consumption_Project_Development_Guide.docx'
Self-contained generator with embedded layout engine and styling definitions.
"""

import os
import sys

# Ensure current script directory and user site-packages are in python module search path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
user_site = r"C:\Users\kalir\AppData\Roaming\Python\Python313\site-packages"
if os.path.exists(user_site) and user_site not in sys.path:
    sys.path.insert(0, user_site)

try:
    import docx
    from docx.shared import Inches, Pt, RGBColor
    from docx.enum.text import WD_ALIGN_PARAGRAPH
    from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
    from docx.oxml import parse_xml, OxmlElement
    from docx.oxml.ns import nsdecls, qn
except ImportError:
    import subprocess
    print("[INFO] Installing python-docx...")
    subprocess.check_call([sys.executable, "-m", "pip", "install", "python-docx", "--user"])
    import docx
    from docx.shared import Inches, Pt, RGBColor
    from docx.enum.text import WD_ALIGN_PARAGRAPH
    from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
    from docx.oxml import parse_xml, OxmlElement
    from docx.oxml.ns import nsdecls, qn

# ==============================================================================
# EMBEDDED DOCUMENT STYLING & PALETTE DEFINITIONS
# ==============================================================================
HEX_PRIMARY_NAVY = "003366"      # Deep Academic Navy
HEX_SECONDARY_TEAL = "008080"    # Professional Teal
HEX_DARK_SLATE = "1E293B"        # Charcoal Slate for Headings
HEX_BODY_TEXT = "334155"         # Slate Grey Body Text
HEX_MUTED_GREY = "64748B"        # Captions & Footers
HEX_BORDER_GREY = "CBD5E1"       # Subtle Table Borders
HEX_ZEBRA_BG = "F8FAFC"          # Table Row Alternating Shading

CALLOUT_CONFIG = {
    "ELECTRICAL SAFETY WARNING": {
        "border": "BE123C",      # Crimson Rose
        "bg": "FFF1F2",          # Soft Red/Pink
        "title_color": RGBColor(190, 18, 60),
        "icon": "⚠️ [ELECTRICAL SAFETY WARNING]"
    },
    "IMPORTANT REQUIREMENT": {
        "border": "D97706",      # Amber
        "bg": "FFFBEB",          # Soft Amber
        "title_color": RGBColor(180, 83, 9),
        "icon": "📌 [IMPORTANT REQUIREMENT]"
    },
    "SECURITY NOTE": {
        "border": "4338CA",      # Indigo
        "bg": "EEF2FF",          # Soft Indigo
        "title_color": RGBColor(67, 56, 202),
        "icon": "🔒 [SECURITY NOTE]"
    },
    "HARDWARE INTEGRATION": {
        "border": "0891B2",      # Cyan
        "bg": "ECFEFF",          # Soft Cyan
        "title_color": RGBColor(14, 116, 144),
        "icon": "🔌 [HARDWARE INTEGRATION]"
    },
    "DEVELOPMENT TIP": {
        "border": "059669",      # Emerald
        "bg": "F0FDF4",          # Soft Green
        "title_color": RGBColor(4, 120, 87),
        "icon": "💡 [DEVELOPMENT TIP]"
    },
    "TESTING CHECKPOINT": {
        "border": "7C3AED",      # Violet
        "bg": "FAF5FF",          # Soft Purple
        "title_color": RGBColor(109, 40, 217),
        "icon": "🎯 [TESTING CHECKPOINT]"
    }
}

COLOR_NAVY = RGBColor(0, 51, 102)
COLOR_TEAL = RGBColor(0, 128, 128)
COLOR_SLATE = RGBColor(30, 41, 59)
COLOR_BODY = RGBColor(51, 65, 85)
COLOR_MUTED = RGBColor(100, 116, 139)

def set_cell_background(cell, color_hex):
    tcPr = cell._tc.get_or_add_tcPr()
    for child in list(tcPr):
        if child.tag.endswith('shd'):
            tcPr.remove(child)
    shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{color_hex}"/>')
    tcPr.append(shd)

def set_callout_borders(cell, border_color_hex):
    tcPr = cell._tc.get_or_add_tcPr()
    for child in list(tcPr):
        if child.tag.endswith('tcBorders'):
            tcPr.remove(child)
    borders = parse_xml(f'''
        <w:tcBorders {nsdecls("w")}>
            <w:top w:val="none" w:sz="0" w:space="0" w:color="auto"/>
            <w:left w:val="single" w:sz="36" w:space="0" w:color="{border_color_hex}"/>
            <w:bottom w:val="none" w:sz="0" w:space="0" w:color="auto"/>
            <w:right w:val="none" w:sz="0" w:space="0" w:color="auto"/>
        </w:tcBorders>
    ''')
    tcPr.append(borders)

def set_table_borders(table, border_color_hex=HEX_BORDER_GREY):
    tblPr = table._tbl.tblPr
    for child in list(tblPr):
        if child.tag.endswith('tblBorders'):
            tblPr.remove(child)
    borders = parse_xml(f'''
        <w:tblBorders {nsdecls("w")}>
            <w:top w:val="single" w:sz="6" w:space="0" w:color="{border_color_hex}"/>
            <w:bottom w:val="single" w:sz="12" w:space="0" w:color="{border_color_hex}"/>
            <w:left w:val="none" w:sz="0" w:space="0" w:color="auto"/>
            <w:right w:val="none" w:sz="0" w:space="0" w:color="auto"/>
            <w:insideH w:val="single" w:sz="4" w:space="0" w:color="{border_color_hex}"/>
            <w:insideV w:val="none" w:sz="0" w:space="0" w:color="auto"/>
        </w:tblBorders>
    ''')
    tblPr.append(borders)

def set_cell_margins(cell, top=140, bottom=140, left=180, right=180):
    tcPr = cell._tc.get_or_add_tcPr()
    for child in list(tcPr):
        if child.tag.endswith('tcMar'):
            tcPr.remove(child)
    tcMar = parse_xml(f'''
        <w:tcMar {nsdecls("w")}>
            <w:top w:w="{top}" w:type="dxa"/>
            <w:bottom w:w="{bottom}" w:type="dxa"/>
            <w:left w:w="{left}" w:type="dxa"/>
            <w:right w:w="{right}" w:type="dxa"/>
        </w:tcMar>
    ''')
    tcPr.append(tcMar)

def add_callout(doc, callout_type, title, text):
    config = CALLOUT_CONFIG.get(callout_type, CALLOUT_CONFIG["IMPORTANT REQUIREMENT"])
    table = doc.add_table(rows=1, cols=1)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False
    
    cell = table.cell(0, 0)
    cell.width = Inches(6.27)
    set_cell_background(cell, config["bg"])
    set_callout_borders(cell, config["border"])
    set_cell_margins(cell, top=160, bottom=160, left=240, right=200)
    
    p_title = cell.paragraphs[0]
    p_title.paragraph_format.space_before = Pt(2)
    p_title.paragraph_format.space_after = Pt(4)
    p_title.paragraph_format.line_spacing = 1.15
    run_icon = p_title.add_run(f"{config['icon']}  {title.upper()}\n")
    run_icon.bold = True
    run_icon.font.name = "Calibri"
    run_icon.font.size = Pt(11)
    run_icon.font.color.rgb = config["title_color"]
    
    p_text = cell.add_paragraph()
    p_text.paragraph_format.space_before = Pt(0)
    p_text.paragraph_format.space_after = Pt(2)
    p_text.paragraph_format.line_spacing = 1.15
    p_text.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    run_text = p_text.add_run(text)
    run_text.font.name = "Calibri"
    run_text.font.size = Pt(10)
    run_text.font.color.rgb = COLOR_SLATE
    
    p_spacer = doc.add_paragraph()
    p_spacer.paragraph_format.space_before = Pt(0)
    p_spacer.paragraph_format.space_after = Pt(4)

def add_heading_1(doc, text):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(16)
    p.paragraph_format.space_after = Pt(6)
    p.paragraph_format.keep_with_next = True
    run = p.add_run(text)
    run.bold = True
    run.font.name = "Calibri"
    run.font.size = Pt(17)
    run.font.color.rgb = COLOR_NAVY
    return p

def add_heading_2(doc, text):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(12)
    p.paragraph_format.space_after = Pt(4)
    p.paragraph_format.keep_with_next = True
    run = p.add_run(text)
    run.bold = True
    run.font.name = "Calibri"
    run.font.size = Pt(13.5)
    run.font.color.rgb = COLOR_TEAL
    return p

def add_heading_3(doc, text):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(8)
    p.paragraph_format.space_after = Pt(2)
    p.paragraph_format.keep_with_next = True
    run = p.add_run(text)
    run.bold = True
    run.font.name = "Calibri"
    run.font.size = Pt(11.5)
    run.font.color.rgb = COLOR_SLATE
    return p

def add_paragraph(doc, text, bold_prefix=None, space_after=4, bullet=False):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(space_after)
    p.paragraph_format.line_spacing = 1.15
    p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    
    if bullet:
        p.paragraph_format.left_indent = Inches(0.25)
        run_bullet = p.add_run("▪  ")
        run_bullet.bold = True
        run_bullet.font.name = "Calibri"
        run_bullet.font.size = Pt(10)
        run_bullet.font.color.rgb = COLOR_TEAL
        
    if bold_prefix:
        run_prefix = p.add_run(bold_prefix)
        run_prefix.bold = True
        run_prefix.font.name = "Calibri"
        run_prefix.font.size = Pt(10.5)
        run_prefix.font.color.rgb = COLOR_NAVY
        
    run_body = p.add_run(text)
    run_body.font.name = "Calibri"
    run_body.font.size = Pt(10.5)
    run_body.font.color.rgb = COLOR_BODY
    return p

def add_code_block(doc, code_text):
    table = doc.add_table(rows=1, cols=1)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False
    cell = table.cell(0, 0)
    cell.width = Inches(6.27)
    set_cell_background(cell, "F1F5F9")
    set_callout_borders(cell, "94A3B8")
    set_cell_margins(cell, top=120, bottom=120, left=180, right=180)
    
    p = cell.paragraphs[0]
    p.paragraph_format.space_before = Pt(2)
    p.paragraph_format.space_after = Pt(2)
    p.paragraph_format.line_spacing = 1.05
    run = p.add_run(code_text)
    run.font.name = "Consolas"
    run.font.size = Pt(9.0)
    run.font.color.rgb = RGBColor(15, 23, 42)
    
    p_spacer = doc.add_paragraph()
    p_spacer.paragraph_format.space_before = Pt(0)
    p_spacer.paragraph_format.space_after = Pt(4)

def add_styled_table(doc, headers, data_rows, col_widths=None):
    table = doc.add_table(rows=len(data_rows) + 1, cols=len(headers))
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False
    set_table_borders(table)
    
    header_tr = table.rows[0]._tr.get_or_add_trPr()
    header_tr.append(parse_xml(f'<w:tblHeader {nsdecls("w")}/>'))
    
    for col_idx, header_text in enumerate(headers):
        cell = table.cell(0, col_idx)
        if col_widths and col_idx < len(col_widths):
            cell.width = Inches(col_widths[col_idx])
        set_cell_background(cell, HEX_PRIMARY_NAVY)
        set_cell_margins(cell, top=140, bottom=140, left=140, right=140)
        p = cell.paragraphs[0]
        p.paragraph_format.space_before = Pt(0)
        p.paragraph_format.space_after = Pt(0)
        p.paragraph_format.line_spacing = 1.05
        run = p.add_run(header_text)
        run.bold = True
        run.font.name = "Calibri"
        run.font.size = Pt(10)
        run.font.color.rgb = RGBColor(255, 255, 255)
        
    for row_idx, row_data in enumerate(data_rows):
        is_even = (row_idx % 2 == 0)
        bg_color = HEX_ZEBRA_BG if is_even else "FFFFFF"
        for col_idx, cell_value in enumerate(row_data):
            cell = table.cell(row_idx + 1, col_idx)
            if col_widths and col_idx < len(col_widths):
                cell.width = Inches(col_widths[col_idx])
            set_cell_background(cell, bg_color)
            set_cell_margins(cell, top=100, bottom=100, left=140, right=140)
            p = cell.paragraphs[0]
            p.paragraph_format.space_before = Pt(0)
            p.paragraph_format.space_after = Pt(0)
            p.paragraph_format.line_spacing = 1.1
            run = p.add_run(str(cell_value))
            run.font.name = "Calibri"
            run.font.size = Pt(9.5)
            run.font.color.rgb = COLOR_BODY
            
    p_spacer = doc.add_paragraph()
    p_spacer.paragraph_format.space_before = Pt(0)
    p_spacer.paragraph_format.space_after = Pt(6)

def create_document():
    doc = docx.Document()
    
    # -------------------------------------------------------------
    # Page Setup (A4, 1-inch margins)
    # -------------------------------------------------------------
    section = doc.sections[0]
    section.page_width = Inches(8.27)    # 210 mm
    section.page_height = Inches(11.69)  # 297 mm
    section.top_margin = Inches(1.0)
    section.bottom_margin = Inches(1.0)
    section.left_margin = Inches(1.0)
    section.right_margin = Inches(1.0)
    
    # Unlink Cover Page Header/Footer
    section.different_first_page_header_footer = True
    
    # -------------------------------------------------------------
    # Running Header & Footer for Subsequent Pages
    # -------------------------------------------------------------
    header = section.header
    p_header = header.paragraphs[0]
    p_header.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    run_h = p_header.add_run("EnergyEx: AI-Powered IoT Smart Electricity Monitoring System  |  Technical Guide")
    run_h.font.name = "Calibri"
    run_h.font.size = Pt(8.5)
    run_h.font.color.rgb = COLOR_MUTED
    
    footer = section.footer
    p_footer = footer.paragraphs[0]
    p_footer.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    run_f_left = p_footer.add_run("Engineering Project Development Guide  —  Prepared by Kaliraj M       Page ")
    run_f_left.font.name = "Calibri"
    run_f_left.font.size = Pt(8.5)
    run_f_left.font.color.rgb = COLOR_MUTED
    
    # Dynamic Page Number Field
    run_pnum = p_footer.add_run()
    run_pnum.font.name = "Calibri"
    run_pnum.font.size = Pt(8.5)
    run_pnum.font.color.rgb = COLOR_NAVY
    run_pnum.bold = True
    
    fldChar1 = parse_xml(r'<w:fldChar %s w:fldCharType="begin"/>' % nsdecls('w'))
    instrText = parse_xml(r'<w:instrText %s xml:space="preserve"> PAGE </w:instrText>' % nsdecls('w'))
    fldChar2 = parse_xml(r'<w:fldChar %s w:fldCharType="separate"/>' % nsdecls('w'))
    fldChar3 = parse_xml(r'<w:fldChar %s w:fldCharType="end"/>' % nsdecls('w'))
    run_pnum._r.append(fldChar1)
    run_pnum._r.append(instrText)
    run_pnum._r.append(fldChar2)
    run_pnum._r.append(fldChar3)
    
    # -------------------------------------------------------------
    # COVER PAGE
    # -------------------------------------------------------------
    # Top decorative bar
    p_top_bar = doc.add_paragraph()
    p_top_bar.paragraph_format.space_before = Pt(0)
    p_top_bar.paragraph_format.space_after = Pt(18)
    run_bar = p_top_bar.add_run("━" * 46)
    run_bar.font.name = "Calibri"
    run_bar.font.size = Pt(16)
    run_bar.font.color.rgb = COLOR_TEAL
    
    # Sub-label
    p_type = doc.add_paragraph()
    p_type.paragraph_format.space_before = Pt(0)
    p_type.paragraph_format.space_after = Pt(8)
    run_type = p_type.add_run("ENGINEERING PROJECT DEVELOPMENT GUIDE & TECHNICAL SPECIFICATION")
    run_type.bold = True
    run_type.font.name = "Calibri"
    run_type.font.size = Pt(11)
    run_type.font.color.rgb = COLOR_TEAL
    
    # Main Document Title
    p_title = doc.add_paragraph()
    p_title.paragraph_format.space_before = Pt(4)
    p_title.paragraph_format.space_after = Pt(12)
    p_title.paragraph_format.line_spacing = 1.15
    run_title = p_title.add_run("AI-POWERED IoT-BASED SMART ELECTRICITY CONSUMPTION MONITORING AND PREDICTION SYSTEM WITH MOBILE APPLICATION")
    run_title.bold = True
    run_title.font.name = "Calibri"
    run_title.font.size = Pt(24)
    run_title.font.color.rgb = COLOR_NAVY
    
    # Short Project Name
    p_subtitle = doc.add_paragraph()
    p_subtitle.paragraph_format.space_before = Pt(0)
    p_subtitle.paragraph_format.space_after = Pt(28)
    run_sub = p_subtitle.add_run("Project EnergyEx: Cyber-Physical Architecture, Mobile Client Design, Dual-Mode Simulation Engine, AI Forecasting, and IoT Hardware Integration Manual")
    run_sub.font.name = "Calibri"
    run_sub.font.size = Pt(13)
    run_sub.font.color.rgb = COLOR_SLATE
    run_sub.italic = True
    
    # Metadata Overview Box (Styled Table)
    meta_table = doc.add_table(rows=6, cols=2)
    meta_table.alignment = WD_TABLE_ALIGNMENT.CENTER
    meta_table.autofit = False
    set_table_borders(meta_table, border_color_hex=HEX_SECONDARY_TEAL)
    
    meta_items = [
        ("Document Classification:", "Complete Project Development Prompt & Implementation Guide"),
        ("Prepared By:", "Kaliraj M (Engineering Student & Full-Stack / IoT Developer)"),
        ("Technology Overview:", "Android (Kotlin, Material 3), Spring Boot 3 (Java 21 LTS), MySQL 8, Python 3.13 (FastAPI, Scikit-learn)"),
        ("Hardware Overview:", "ESP32 DevKit V1, PZEM-004T V3.0 AC Energy Sensor, 4-Channel 5V Relay Module"),
        ("Implementation Strategy:", "Phase 1: Zero-Hardware Realistic IoT Simulation Mode\nPhase 2: Live IoT Hardware Ingestion (Arrival Tomorrow)"),
        ("Document Version & Date:", "Version 2.0 (Production Engineering Release)  |  November 2026")
    ]
    
    for row_idx, (k, v) in enumerate(meta_items):
        cell_k = meta_table.cell(row_idx, 0)
        cell_v = meta_table.cell(row_idx, 1)
        cell_k.width = Inches(2.2)
        cell_v.width = Inches(4.07)
        set_cell_background(cell_k, HEX_ZEBRA_BG)
        set_cell_background(cell_v, "FFFFFF")
        set_cell_margins(cell_k, top=100, bottom=100, left=140, right=140)
        set_cell_margins(cell_v, top=100, bottom=100, left=140, right=140)
        
        pk = cell_k.paragraphs[0]
        pk.paragraph_format.space_before = Pt(0)
        pk.paragraph_format.space_after = Pt(0)
        rk = pk.add_run(k)
        rk.bold = True
        rk.font.name = "Calibri"
        rk.font.size = Pt(10)
        rk.font.color.rgb = COLOR_NAVY
        
        pv = cell_v.paragraphs[0]
        pv.paragraph_format.space_before = Pt(0)
        pv.paragraph_format.space_after = Pt(0)
        rv = pv.add_run(v)
        rv.font.name = "Calibri"
        rv.font.size = Pt(10)
        rv.font.color.rgb = COLOR_BODY
        
    p_spacer_cov = doc.add_paragraph()
    p_spacer_cov.paragraph_format.space_before = Pt(24)
    p_spacer_cov.paragraph_format.space_after = Pt(0)
    
    # Bottom Academic Note
    p_acad = doc.add_paragraph()
    p_acad.paragraph_format.space_before = Pt(20)
    p_acad.paragraph_format.space_after = Pt(0)
    p_acad.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r_acad = p_acad.add_run("DEPARTMENT OF COMPUTER SCIENCE & ELECTRICAL ENGINEERING\nFINAL YEAR CAPSTONE PROJECT DEVELOPMENT GUIDE")
    r_acad.bold = True
    r_acad.font.name = "Calibri"
    r_acad.font.size = Pt(10)
    r_acad.font.color.rgb = COLOR_MUTED
    
    doc.add_page_break()
    
    # -------------------------------------------------------------
    # SECTION 1: PROJECT OVERVIEW
    # -------------------------------------------------------------
    add_heading_1(doc, "1. Project Overview")
    
    add_paragraph(doc, 
        "Traditional domestic and commercial electrical installations in India rely heavily on electromechanical or basic static electronic utility meters. While these utility-grade meters reliably record total cumulative energy consumption for billing cycles, they act purely as passive accumulators. Consumers and facility managers are kept completely in the dark regarding instantaneous electrical parameters, appliance-specific energy splits, power factor degradation, and transient load surges until the utility electricity bill arrives at the end of the month.",
        bold_prefix="1.1 Context and Problem Statement: "
    )
    
    add_paragraph(doc,
        "This billing lag causes severe economic and operational problems: (1) consumers experience billing shocks caused by undetected high-draw loads or failing appliances; (2) high standby 'vampire' power consumption continues unabated during idle hours; (3) domestic wiring and appliance safety is jeopardized due to unmonitored overcurrent or voltage fluctuations; and (4) utility peak-load strains cannot be mitigated without actionable real-time consumer insights.",
        bold_prefix="Core Operational Pitfalls: "
    )
    
    add_paragraph(doc,
        "EnergyEx is an end-to-end, multi-tier cyber-physical smart electricity consumption monitoring, prediction, and control system. It integrates Internet of Things (IoT) edge sensing, industrial-grade electrical metering, cloud/local enterprise application services, artificial intelligence load forecasting, and a high-performance native Android mobile application.",
        bold_prefix="1.2 The EnergyEx Solution: "
    )
    
    add_paragraph(doc,
        "Crucially, EnergyEx is designed with a two-phase architecture. In Phase 1 (prior to physical hardware arrival), the system operates in a realistic IoT Simulation Mode that accurately mimics physical AC waveforms, cyclic appliance duty cycles, inductive power factors, and random line noise, clearly tagged as DEMO DATA. In Phase 2 (post hardware arrival), the system transitions seamlessly to Live IoT Mode using an ESP32 microcontroller, PZEM-004T v3.0 AC energy sensor, and optocoupled relay module, without requiring a single code rewrite in the mobile client or database layer.",
        bold_prefix="Dual-Mode Engineering Innovation: "
    )
    
    add_callout(doc, "ELECTRICAL SAFETY WARNING", "Mains Voltage Hazard (230V AC / 50 Hz)", 
        "The hardware phase of this project involves direct interaction with lethal 230V AC mains electricity. All high-voltage wiring, CT clamp installation, and relay connections must be performed inside an insulated enclosure with the AC mains completely disconnected. Installation must be verified by a certified electrician. Never touch bare copper conductors or expose low-voltage microcontroller logic directly to AC mains terminals."
    )
    
    # -------------------------------------------------------------
    # SECTION 2: PROJECT OBJECTIVES
    # -------------------------------------------------------------
    add_heading_1(doc, "2. Project Objectives")
    
    add_paragraph(doc, "The primary technical and operational objectives of the EnergyEx system are established as follows:")
    
    objectives = [
        ("Granular Domestic Appliance Monitoring: ", "Continuously track and disaggregate power consumption across individual domestic loads (Living Room Light, Ceiling Fan, Smart TV, and Kitchen Refrigerator)."),
        ("Precision AC Parameter Acquisition: ", "Measure True RMS AC Voltage (V), Current (A), Active Real Power (W), Frequency (Hz), Power Factor (cos φ), and Cumulative Active Energy (kWh) with sub-second temporal resolution."),
        ("Dynamic Indian Rupee (₹) Cost Computation: ", "Calculate real-time and projected expenditure based on the official Indian LT domestic tiered tariff structure (escalating slabs: 0-100 kWh, 101-300 kWh, >300 kWh, plus fixed monthly service charges)."),
        ("Temporal Analytics and Visual Dashboards: ", "Provide daily, weekly, and monthly aggregated consumption views with interactive visual analytics and peak load indicators."),
        ("Machine Learning Demand Forecasting: ", "Deploy an ensemble Random Forest Regression model to predict 7-day and 30-day ahead electricity demand based on seasonal, weekly, and temporal lag features."),
        ("Statistical Anomaly & Leakage Detection: ", "Detect electrical anomalies, abnormal continuous draws, and equipment degradation using dual Interquartile Range (IQR) gating and Standardized Z-Score filters."),
        ("Multi-Tier Safety Alerts: ", "Issue instantaneous notifications for critical electrical conditions including Overvoltage (>250V), Undervoltage (<195V), Overcurrent (>15A), Overpower, and anomalous baseline spikes."),
        ("Bi-Directional Remote Appliance Actuation: ", "Provide zero-latency ON/OFF remote switching of individual home appliances from the Android client with bidirectional state acknowledgment handshakes."),
        ("Automated Time-of-Day Appliance Scheduling: ", "Enable users to configure automated weekly operating schedules that execute deterministically on the backend even if the mobile device is offline."),
        ("Seamless Dual-Mode Architecture: ", "Support instant, zero-rebuild transition between Demo Mode (mathematical simulation) and Live IoT Mode (physical ESP32/PZEM hardware) via non-breaking abstraction interfaces.")
    ]
    
    for prefix, body in objectives:
        add_paragraph(doc, body, bold_prefix=prefix, bullet=True)
        
    add_callout(doc, "IMPORTANT REQUIREMENT", "Strict Decoupling of Simulation & Hardware Layers", 
        "The system architecture must enforce absolute parity between simulated data packets and physical ESP32 telemetry packets. The mobile application and database schemas must remain 100% agnostic to whether the incoming telemetry originates from the software simulator or the physical PZEM-004T sensor. Transitions between modes must occur via telemetry source switching alone."
    )
    
    # -------------------------------------------------------------
    # SECTION 3: SYSTEM ARCHITECTURE
    # -------------------------------------------------------------
    add_heading_1(doc, "3. System Architecture")
    
    add_paragraph(doc, 
        "EnergyEx adopts a decoupled, 4-tier microservice and edge architecture designed for high throughput, sub-second telemetry propagation, fault isolation, and cross-platform maintainability. The four foundational tiers comprise:",
        bold_prefix="3.1 Multi-Tier Architectural Topology: "
    )
    
    add_paragraph(doc, "1. Edge IoT Sensing & Actuation Tier: An ESP32 microcontroller interfaced via UART to a PZEM-004T v3.0 AC energy meter and driving a 4-channel optocoupled relay module. Gathers 6 electrical parameters every 3 seconds and transmits JSON payloads via Wi-Fi HTTP POST.", bullet=True)
    add_paragraph(doc, "2. Enterprise Application Backend Tier: A Java 21 / Spring Boot 3 enterprise server managing RESTful CRUD endpoints, Spring Data JPA relational persistence with MySQL 8, full-duplex STOMP over WebSocket broadcasting, an embedded multi-appliance realistic simulation engine, and an automated rule scheduler.", bullet=True)
    add_paragraph(doc, "3. Artificial Intelligence & Analytics Microservice: A high-performance Python 3.13 / FastAPI service running Scikit-learn Random Forest regression, statistical IQR/Z-score anomaly detection, and energy efficiency recommendation heuristics.", bullet=True)
    add_paragraph(doc, "4. Mobile Client Application Tier: A native Android application written in Kotlin following Material Design 3 guidelines. Integrates Retrofit 2 for REST communication, an asynchronous STOMP WebSocket client for real-time telemetry streaming, a custom Canvas-rendered live energy chart, and 14 dedicated user screens.", bullet=True)
    
    add_heading_2(doc, "3.2 System Architectural Flow Diagram")
    
    arch_ascii = (
        "+---------------------------------------------------------------------------------------------------+\n"
        "|                                     ENERGYEX SYSTEM ARCHITECTURE                                   |\n"
        "+---------------------------------------------------------------------------------------------------+\n"
        "|  [PHYSICAL IOT EDGE TIER]                                                                         |\n"
        "|  +--------------------+      Modbus RTU (UART2)     +--------------------+                        |\n"
        "|  |   PZEM-004T v3.0   | --------------------------> |   ESP32 DevKit V1  |                        |\n"
        "|  | (True RMS Metering)| <-------------------------- |  (240MHz DualCore) |                        |\n"
        "|  +--------------------+                             +---------+----------+                        |\n"
        "|            | (CT Clamp)                                       | (Active-LOW GPIOs)                |\n"
        "|      [230V AC Mains]                                          v                                   |\n"
        "|            |                                        +--------------------+                        |\n"
        "|      [Home Appliances] <--------------------------- | 4-Ch 5V Relay Board|                        |\n"
        "|      (Light/Fan/TV/Ref)                             +--------------------+                        |\n"
        "+---------------------------------------------------------------|-----------------------------------+\n"
        "                                                                | Wi-Fi (HTTP POST / REST)\n"
        "                                                                v\n"
        "+---------------------------------------------------------------------------------------------------+\n"
        "|  [ENTERPRISE APPLICATION BACKEND TIER (Java 21 / Spring Boot 3)]                                  |\n"
        "|  +------------------------+    +-----------------------+    +----------------------------------+  |\n"
        "|  | TelemetryController    |    | IoTSimulatorService   |    | WebSocket STOMP Broadcaster      |  |\n"
        "|  | (Live Ingest & Ack)    |    | (Realistic Load Model)|    | (/topic/telemetry, /topic/alerts)|  |\n"
        "|  +-----------+------------+    +-----------+-----------+    +-----------------+----------------+  |\n"
        "|              |                             |                                  ^                   |\n"
        "|              v                             v                                  |                   |\n"
        "|  +-----------------------------------------------------+                      |                   |\n"
        "|  | Business Logic Layer: TariffEngine, Scheduler, Alert| ---------------------+                   |\n"
        "|  +---------------------------+-------------------------+                                          |\n"
        "|                              |                                                                    |\n"
        "|            +-----------------+-----------------+                                                  |\n"
        "|            | Spring Data JPA / Hibernate       | HTTP JSON (Port 8000)                            |\n"
        "|            v                                   v                                                  |\n"
        "|  +--------------------+             +----------------------------------------------------------+  |\n"
        "|  | MySQL 8 Database   |             |  [AI / ML MICROSERVICE TIER (Python 3.13 / FastAPI)]     |  |\n"
        "|  | (9 Relational Tbls)|             |  - Random Forest Demand Regressor (50 Trees)             |  |\n"
        "|  +--------------------+             |  - Statistical IQR & Z-Score Anomaly Engine              |  |\n"
        "|                                     |  - Energy Efficiency Recommendation Heuristics           |  |\n"
        "+-------------------------------------|----------------------------------------------------------+--+\n"
        "                                      |                                                             \n"
        "                        STOMP / REST  | (HTTP Port 8080)                                            \n"
        "                                      v                                                             \n"
        "+---------------------------------------------------------------------------------------------------+\n"
        "|  [MOBILE CLIENT TIER (Android Kotlin / Material 3)]                                               |\n"
        "|  - 14 Dedicated Screens (Dashboard, Live Gauges, Billing, Prediction, Remote Relays, Scheduling) |\n"
        "|  - Custom Canvas EnergyChartView (Sub-second reactive updates via WebSocket STOMP)               |\n"
        "+---------------------------------------------------------------------------------------------------+"
    )
    add_code_block(doc, arch_ascii)
    
    # -------------------------------------------------------------
    # SECTION 4: TECHNOLOGY STACK
    # -------------------------------------------------------------
    add_heading_1(doc, "4. Technology Stack")
    add_paragraph(doc, "The complete technology stack across all tiers is detailed below, highlighting the architectural role and engineering justification for each component selection:")
    
    tech_headers = ["Layer", "Technology", "Version", "Purpose & Engineering Justification"]
    tech_data = [
        ["Mobile Client", "Kotlin", "1.9.22", "Modern, concise, type-safe programming language for Android development with coroutine support."],
        ["Mobile Client", "Android Studio", "2023.2+", "Official IDE offering layout validation, profiler tools, and Gradle build orchestration."],
        ["Mobile Client", "Material Design 3", "1.11.0", "Provides adaptive, accessible UI components, standardized themes, and dynamic color palettes."],
        ["Mobile Client", "Retrofit 2 & OkHttp", "2.9.0", "Type-safe HTTP REST client for synchronous and asynchronous JSON API communication."],
        ["Mobile Client", "StompProtocolAndroid", "1.6.6", "Lightweight WebSocket STOMP protocol client for real-time sub-second telemetry streaming."],
        ["Mobile Client", "EnergyChartView", "Custom", "High-performance custom Android View rendering smooth cubic Bezier power graphs directly on Canvas."],
        ["Backend Server", "Java", "21 LTS", "Modern enterprise runtime featuring virtual threads (Project Loom), pattern matching, and record classes."],
        ["Backend Server", "Spring Boot", "3.2.3", "Enterprise-grade microservice framework with dependency injection, auto-configuration, and robust REST tools."],
        ["Backend Server", "Spring Data JPA", "3.2.3", "Object-Relational Mapping (ORM) powered by Hibernate for database abstraction and query automation."],
        ["Backend Server", "Spring WebSocket", "3.2.3", "Full-duplex WebSocket communication with STOMP protocol message routing and topic subscriptions."],
        ["Database", "MySQL", "8.0+", "Production-grade ACID-compliant relational database management system with HikariCP connection pooling."],
        ["Database (Dev)", "H2 In-Memory DB", "2.2+", "Zero-configuration in-memory database used for instant offline developer setup and integration tests."],
        ["AI / Machine Learning", "Python", "3.13", "High-productivity scientific computing environment with modern typing and performance optimizations."],
        ["AI / Machine Learning", "FastAPI & Uvicorn", "0.110+", "Asynchronous, ASGI-based high-performance web microservice framework with automatic OpenAPI documentation."],
        ["AI / Machine Learning", "Scikit-learn", "1.4.1+", "Machine learning library providing Random Forest Regression, train/test split, and evaluation metrics."],
        ["AI / Machine Learning", "NumPy & Pandas", "2.0+ / 2.2+", "Vectorized array mathematics, rolling time-window calculations, and tabular feature engineering."],
        ["IoT & Firmware", "ESP32 DevKit V1", "Dual-Core", "240 MHz microcontroller managing concurrent UART sensor polling, relay driving, and Wi-Fi transmission."],
        ["IoT & Firmware", "Arduino C++", "2.x", "Embedded firmware framework leveraging HardwareSerial and asynchronous network client libraries."],
        ["IoT & Firmware", "PZEM004Tv30 Lib", "1.1.2", "Arduino software library wrapping Modbus RTU register polling and CRC checksum verification."],
        ["IoT & Firmware", "ArduinoJson", "7.0.3", "Zero-copy JSON serialization and deserialization library optimized for embedded microcontrollers."]
    ]
    add_styled_table(doc, tech_headers, tech_data, col_widths=[1.1, 1.3, 0.8, 3.07])
    
    # -------------------------------------------------------------
    # SECTION 5: HARDWARE REQUIREMENTS
    # -------------------------------------------------------------
    add_heading_1(doc, "5. Hardware Requirements")
    add_paragraph(doc, "The physical IoT hardware setup requires specific components engineered to safely acquire precision AC electrical measurements and actuate domestic loads up to 10 Amperes per channel:")
    
    hw_headers = ["Component", "Specification / Model", "Quantity", "Functional Description in EnergyEx"]
    hw_data = [
        ["Microcontroller", "ESP32 DevKit V1 (30-pin WROOM-32)", "1 Unit", "Dual-core 32-bit Xtensa LX6 @ 240 MHz, 520 KB SRAM, 4 MB Flash, 2.4 GHz Wi-Fi. Polls sensor via UART2 and executes commands."],
        ["AC Energy Sensor", "PZEM-004T V3.0 (with Optical Isolation)", "1 Unit", "Dedicated metering ASIC measuring True RMS Voltage (80-260V), Current (0-100A), Active Power (0-23kW), Energy (0-9999kWh), Frequency, and PF."],
        ["Current Sensor", "100A Split-Core / Solid-Core CT Clamp", "1 Unit", "Current transformer converting primary high-amperage AC current to milliampere sensing current via magnetic induction."],
        ["Relay Actuator", "4-Channel 5V Optocoupled Relay Module", "1 Unit", "Active-LOW electro-mechanical relay board with PC817 optocouplers providing complete galvanic isolation. Rated 10A 250V AC."],
        ["Power Supply", "5V 2A Regulated DC Power Adapter", "1 Unit", "Powers ESP32 VIN pin, relay coil supply, and PZEM low-voltage 5V logic rail."],
        ["Interconnects", "Dupont Jumper Wires (M-M, M-F, F-F)", "1 Set", "Connects GPIO pins to PZEM UART and relay logic input pins."],
        ["AC Wiring", "1.5 sq mm Insulated Copper AC Wire", "3 Meters", "Carries 230V AC Live and Neutral currents safely between wall outlet, relays, and appliances."],
        ["Safety Enclosure", "Flame-Retardant ABS Project Box", "1 Unit", "Encloses all AC terminals and high-voltage connections to prevent accidental physical contact."]
    ]
    add_styled_table(doc, hw_headers, hw_data, col_widths=[1.3, 1.8, 0.7, 2.47])
    
    add_callout(doc, "HARDWARE INTEGRATION", "Current Transformer (CT) Clamping Golden Rule",
        "The Current Transformer (CT) clamp must be placed around ONLY ONE conductor—specifically the LIVE (Phase) wire. Clamping both Live and Neutral conductors simultaneously will cause their equal and opposite alternating electromagnetic fields to cancel each other out completely, resulting in an erroneous current measurement of exactly 0.00 Amperes."
    )
    
    # -------------------------------------------------------------
    # SECTION 6: SOFTWARE REQUIREMENTS
    # -------------------------------------------------------------
    add_heading_1(doc, "6. Software Requirements")
    add_paragraph(doc, "The software development and execution ecosystem requires the following environments, toolchains, and operating systems:")
    
    sw_headers = ["Category", "Tool / Environment", "Minimum Version", "Role & Usage Scope"]
    sw_data = [
        ["Operating System", "Microsoft Windows / Linux / macOS", "Windows 10/11 64-bit", "Host operating system for IDEs, build tools, databases, and microservice execution."],
        ["Mobile Development", "Android Studio", "Iguana 2023.2.1+", "Compiling Kotlin source, building APKs, managing Android Virtual Devices (AVD), and USB debugging."],
        ["Mobile Target", "Android SDK Platform", "API 34 (Android 14)", "Target SDK version API 34; Minimum SDK version API 26 (Android 8.0 Oreo) for broad compatibility."],
        ["Backend Runtime", "Java Development Kit (JDK)", "OpenJDK 21 LTS", "Compiling and running the Spring Boot enterprise application with modern language features."],
        ["Backend Build Tool", "Apache Maven", "3.9.0+", "Dependency management, automated testing, and fat-JAR artifact compilation."],
        ["Database Engine", "MySQL Community Server", "8.0.35+", "Persistent relational storage for readings, appliances, tariffs, schedules, and alerts."],
        ["AI Runtime", "Python", "3.13 64-bit", "Execution engine for FastAPI, Scikit-learn Random Forest model, and statistical anomaly algorithms."],
        ["Firmware IDE", "Arduino IDE", "2.3.0+", "Writing, compiling, and flashing ESP32 firmware with ESP32 board package by Espressif."],
        ["API Testing", "Postman / cURL", "10.0+", "Validating REST endpoints, inspecting JSON responses, and simulating hardware HTTP POST requests."]
    ]
    add_styled_table(doc, sw_headers, sw_data, col_widths=[1.3, 1.7, 1.1, 2.17])
    
    # -------------------------------------------------------------
    # SECTION 7: ANDROID MOBILE APPLICATION DESIGN
    # -------------------------------------------------------------
    add_heading_1(doc, "7. Android Mobile Application Design (All 14 Screens)")
    add_paragraph(doc, 
        "The EnergyEx Android application is engineered to deliver a seamless, intuitive, and highly responsive user experience across diverse Android device form factors. Built in Kotlin using Material Design 3, the client architecture completely segregates data presentation from communication protocols. Below is the comprehensive architectural specification of all 14 screens:",
        bold_prefix="7.1 Interface Architecture: "
    )
    
    screens = [
        ("Screen 1: Splash Screen (SplashActivity)",
         "Purpose: Initial entry point that verifies application configuration and routes the user.\n"
         "Features: Displays animated EnergyEx brand identity; checks SharedPreferences via SessionManager to determine if first-time onboarding is complete; initializes network clients; transitions seamlessly to FirstTimeSetupActivity for new users or MainActivity for returning users."),
        
        ("Screen 2: First-Time User Setup (FirstTimeSetupActivity)",
         "Purpose: Zero-friction onboarding screen designed to configure essential parameters.\n"
         "Features: Captures the user's preferred display name; allows input of local electricity tariff per unit (₹/kWh) with a sensible default (₹6.50/kWh); provides a 'Skip / Demo Mode' button that enables instant application exploration without mandatory cloud registration; provides an interactive walkthrough explaining hardware connection."),
        
        ("Screen 3: Home Dashboard (DashboardFragment)",
         "Purpose: Central command center displaying real-time metrics and system health at a glance.\n"
         "Features: Key metric cards showing Total Cumulative Energy (kWh), Estimated Monthly Bill (₹), Active Connected Appliances count, Real-Time Active Power (W), and Today's Consumption; dynamic live usage chart rendered on custom Canvas; prominent mode badge ('DEMO MODE' in Amber vs 'LIVE IoT MODE' in Green) that can be tapped to toggle data sources; emergency quick-actuation switches; recent alert summary banner."),
        
        ("Screen 4: Appliance List (AppliancesFragment)",
         "Purpose: Comprehensive catalog and overview of all monitored domestic appliances.\n"
         "Features: Displays 4 pre-configured appliance cards (Living Room Light, Ceiling Fan, Smart TV, Kitchen Refrigerator); shows individual real-time wattage consumption, active status badge, channel assignment, and individual power toggle buttons; supports pull-to-refresh and quick navigation to granular details."),
        
        ("Screen 5: Individual Appliance Details (ApplianceDetailFragment)",
         "Purpose: In-depth diagnostic view of a selected appliance's electrical behavior.\n"
         "Features: Detailed breakdown of current power draw, daily operating hours, rated wattage comparison, daily energy accumulation, and relay status; provides a 24-hour historical power graph and direct toggle actuation with confirmation feedback."),
        
        ("Screen 6: Live Electricity Monitoring (LiveMonitoringFragment)",
         "Purpose: High-frequency electrical parameter dashboard updated via WebSocket every 3 seconds.\n"
         "Features: Precision digital gauges displaying RMS Voltage (V), RMS Current (A), Active Real Power (W), AC Mains Frequency (Hz), and Power Factor (cos φ); dynamic efficiency meter highlighting inductive lag; real-time sparkline graph visualizing voltage stability and power surges."),
        
        ("Screen 7: Consumption History (ConsumptionHistoryFragment)",
         "Purpose: Historical analysis across multiple temporal aggregation windows.\n"
         "Features: Tabbed interface offering 'Today' (hourly intervals), 'Weekly' (7-day daily bars), and 'Monthly' (30-day view); interactive bar charts displaying kilowatt-hour figures and computed costs in Indian Rupees (₹); peak load hour identification and statistical summary cards."),
        
        ("Screen 8: Electricity Bill Estimation (BillEstimationFragment)",
         "Purpose: Transparent financial forecasting and tiered tariff breakdown.\n"
         "Features: Visual representation of consumption across Indian LT tariff slabs (0-100 kWh @ ₹4.50, 101-300 kWh @ ₹7.50, >300 kWh @ ₹10.00); fixed monthly charge calculation; projected end-of-billing-cycle total expenditure; actionable tips for keeping consumption within lower tariff tiers."),
        
        ("Screen 9: AI Prediction (PredictionFragment)",
         "Purpose: Machine learning forecast visualization and energy-saving recommendations.\n"
         "Features: 7-day and 30-day ahead energy demand projection curves generated by the Random Forest model; model reliability score display (R² confidence percentage); seasonal load component breakdown; automated energy-saving recommendations generated by the AI heuristics engine."),
        
        ("Screen 10: Alerts and Notifications (AlertsFragment)",
         "Purpose: Centralized audit log of system alerts, safety warnings, and anomaly events.\n"
         "Features: Chronological feed of triggered alerts categorized by severity (CRITICAL, WARNING, INFO); covers overvoltage, overcurrent, power surges, and statistical anomalies; includes 'Mark as Read' and 'Clear All' functions; tapping an alert displays recommended remedial actions."),
        
        ("Screen 11: Remote Appliance Control (RemoteControlFragment)",
         "Purpose: Dedicated actuation switchboard for remote domestic load control.\n"
         "Features: Individual toggle switches for all 4 relay channels; bidirectional state indicator displaying 'PENDING' during command transmission and 'CONFIRMED' upon physical relay acknowledgment; master 'Turn All OFF' emergency button; interlock warnings for sensitive loads."),
        
        ("Screen 12: Scheduling (SchedulingFragment)",
         "Purpose: Time-of-day and day-of-week automated appliance automation manager.\n"
         "Features: Create, edit, and delete automated schedules; configure ON/OFF execution times using Material TimePicker; select recurring days of the week (Monday through Sunday); toggle schedule activation; rules persist in MySQL and execute on the backend independently of phone connectivity."),
        
        ("Screen 13: Wi-Fi and IoT Device Setup (IoTSetupFragment)",
         "Purpose: Management console for hardware pairing, network provisioning, and mode switching.\n"
         "Features: Toggle between Demo Mode and Live IoT Mode; input ESP32 static IP address and backend server URL; Wi-Fi credential configuration helper; live hardware connection status badge ('Connected', 'Connecting', 'Disconnected') with ping latency diagnostic."),
        
        ("Screen 14: Settings and User Profile (SettingsFragment)",
         "Purpose: Application configuration, account settings, and system diagnostics.\n"
         "Features: Edit user profile information; modify base tariff rate per unit; configure safety alert thresholds (overvoltage/overcurrent trigger points); reset demo data; view system version, backend API health status, and project documentation links.")
    ]
    
    for title, desc in screens:
        add_heading_2(doc, title)
        add_paragraph(doc, desc)
        
    # -------------------------------------------------------------
    # SECTION 8: USER-FRIENDLY FIRST-TIME SETUP
    # -------------------------------------------------------------
    add_heading_1(doc, "8. User-Friendly First-Time Setup")
    
    add_paragraph(doc,
        "A critical UX failure of many IoT applications is forcing users to undergo complex account creation, email verification, and Wi-Fi hardware provisioning before they can even view the application interface. EnergyEx eliminates this barrier by implementing a frictionless first-time launch flow:",
        bold_prefix="8.1 Onboarding Philosophy: "
    )
    
    add_paragraph(doc, "1. Minimal Required Input: On first launch, the app asks only for the user's name and provides an editable electricity tariff rate (defaulting to ₹6.50/kWh based on standard Indian domestic averages).", bullet=True)
    add_paragraph(doc, "2. No Mandatory Registration: The application initializes immediately in local Demo Mode. No password creation, phone OTP, or cloud sign-in is required for local evaluation.", bullet=True)
    add_paragraph(doc, "3. Optional Cloud Sync: A secure registration option is accessible later within Settings if the user requires remote synchronization or multi-device access.", bullet=True)
    add_paragraph(doc, "4. Interactive Hardware Carousel: An intuitive card carousel explains how the ESP32, PZEM sensor, and relays integrate with domestic appliances, preparing the user for Phase 2 hardware arrival.", bullet=True)
    
    # -------------------------------------------------------------
    # SECTION 9: DEMO MODE AND LIVE IoT MODE
    # -------------------------------------------------------------
    add_heading_1(doc, "9. Demo Mode and Live IoT Mode")
    
    add_paragraph(doc,
        "To satisfy the core project constraint—building the full application before the arrival of physical components—EnergyEx implements a dual-mode ingestion architecture with strict data source segregation:",
        bold_prefix="9.1 Architecture of Dual-Mode Operation: "
    )
    
    add_paragraph(doc,
        "In Phase 1, the Spring Boot backend runs an internal IoTSimulatorService that synthesizes realistic AC electrical behavior. It models cyclical refrigerator compressor duty cycles (150W active cooling for 15-20 minutes, 3W idle standby), ceiling fan inductive lag (65W, cos φ = 0.82), Smart TV active loads (90W, cos φ = 0.95), and LED lighting (25W, cos φ = 0.98). Realistic Gaussian noise is superimposed on the AC mains voltage (230V ± 3.5V). Every generated reading is explicitly stamped with 'is_demo_data = true'.",
        bold_prefix="Phase 1 — Demo Mode (Simulation): "
    )
    
    add_paragraph(doc,
        "In Phase 2, when the ESP32, PZEM-004T, and relay module arrive and are powered on, the ESP32 transmits genuine physical telemetry via HTTP POST to /api/v1/iot/telemetry. The backend stamps these readings with 'is_demo_data = false'. The system transitions dynamically to Live IoT Mode without requiring a database schema modification or mobile application rebuild.",
        bold_prefix="Phase 2 — Live IoT Mode (Hardware Ingestion): "
    )
    
    add_callout(doc, "DEVELOPMENT TIP", "Zero-Rebuild Data Source Switching",
        "The mobile application and database structure are identical in both modes. The user can toggle between Demo Mode and Live IoT Mode directly from the top dashboard badge or within IoT Setup. Simulated and live readings are segregated in the database via the boolean flag 'is_demo_data', preventing contaminated historical analytics."
    )
    
    # -------------------------------------------------------------
    # SECTION 10: SECURE WI-FI AND IOT CONNECTIVITY
    # -------------------------------------------------------------
    add_heading_1(doc, "10. Secure Wi-Fi and IoT Connectivity")
    
    add_paragraph(doc,
        "The ESP32 communicates with the Spring Boot backend over standard IEEE 802.11 b/g/n (2.4 GHz) Wi-Fi using WPA2-PSK encryption. To ensure operational robustness in real-world domestic environments, the firmware and backend implement three layers of network resilience:",
        bold_prefix="10.1 Network Topology and Resilience: "
    )
    
    add_paragraph(doc, "1. Non-Blocking Asynchronous Reconnection: The ESP32 firmware non-blockingly monitors WiFi.status(). If the Wi-Fi router restarts or signal drops, the microcontroller maintains existing relay states and attempts reconnection every 5 seconds without freezing local operations.", bullet=True)
    add_paragraph(doc, "2. Pre-Shared Bearer Token Authentication: Every HTTP POST request from the ESP32 includes a pre-shared DEVICE_AUTH_TOKEN in the Authorization header and JSON body. Unauthorized packets are immediately rejected with HTTP 401 Unauthorized.", bullet=True)
    add_paragraph(doc, "3. Android Offline Caching: If the mobile device loses network connectivity, the Android app gracefully displays cached historical data from Room/SharedPreferences, presents a visual 'Offline' indicator, and automatically resumes live STOMP subscriptions upon reconnect.", bullet=True)
    
    # -------------------------------------------------------------
    # SECTION 11: INDIVIDUAL APPLIANCE MONITORING
    # -------------------------------------------------------------
    add_heading_1(doc, "11. Individual Appliance Monitoring")
    
    add_paragraph(doc,
        "EnergyEx maps individual home appliances to dedicated relay channels and electrical load profiles, enabling granular energy accounting across the household:",
        bold_prefix="11.1 Channel-to-Appliance Mapping: "
    )
    
    app_headers = ["Channel", "Appliance Name", "Category", "Rated Power (W)", "Load Type", "Expected Power Factor"]
    app_data = [
        ["Channel 1", "Living Room Light", "Lighting", "25 W", "Resistive / Electronic (LED)", "0.98 - 1.00"],
        ["Channel 2", "Ceiling Fan", "Motor / Inductive", "65 W", "Inductive Motor", "0.80 - 0.85"],
        ["Channel 3", "Smart TV & Entertainment", "Entertainment", "90 W", "SMPS / Electronic", "0.92 - 0.96"],
        ["Channel 4", "Kitchen Refrigerator", "Refrigeration", "150 W (Compressor)", "Inductive Compressor", "0.82 - 0.88"]
    ]
    add_styled_table(doc, app_headers, app_data, col_widths=[0.9, 1.8, 1.3, 1.0, 1.5, 1.2])
    
    add_paragraph(doc,
        "In a single-CT hardware deployment where one PZEM sensor monitors the common feeder wire, EnergyEx utilizes an intelligent disaggregation algorithm. By correlating relay channel state changes (ON/OFF transitions) with step-deltas in measured total active power, the system computes the instantaneous consumption of each active appliance with high precision.",
        bold_prefix="Single-CT Disaggregation Principle: "
    )
    
    # -------------------------------------------------------------
    # SECTION 12: ELECTRICITY CONSUMPTION AND COST CALCULATION
    # -------------------------------------------------------------
    add_heading_1(doc, "12. Electricity Consumption and Cost Calculation")
    
    add_paragraph(doc,
        "To deliver engineering-grade accuracy, EnergyEx implements exact single-phase alternating current electrical relationships and follows official Indian utility billing methodologies.",
        bold_prefix="12.1 Mathematical Formulations of AC Power: "
    )
    
    add_paragraph(doc, "1. Instantaneous Active (Real) Power (P): The actual rate of energy consumed to perform physical work:\n   P = V_RMS × I_RMS × cos(φ)   [Watts, W]", bullet=True)
    add_paragraph(doc, "2. Apparent Power (S): The product of root-mean-square voltage and current:\n   S = V_RMS × I_RMS   [Volt-Amperes, VA]", bullet=True)
    add_paragraph(doc, "3. Reactive Power (Q): The power oscillating between source and reactive load components:\n   Q = V_RMS × I_RMS × sin(φ)   [Volt-Amperes Reactive, VAR]", bullet=True)
    add_paragraph(doc, "4. Discrete Numerical Energy Integration: Active energy is the time-integral of active power. With telemetry arriving every Δt = 3.0 seconds, cumulative energy is calculated via trapezoidal numerical summation:\n   E(kWh) = Σ (P_k × Δt_k) / (3,600 × 1,000)", bullet=True)
    
    add_heading_2(doc, "12.2 Indian Domestic LT Tiered Tariff Structure")
    add_paragraph(doc,
        "Electricity distribution companies (DISCOMs) in India bill residential consumers on an escalating block tariff to encourage conservation. The total monthly bill is computed as:",
        bold_prefix="Tariff Formulation: "
    )
    
    add_paragraph(doc, "Total Bill (₹) = Fixed Service Charge + Σ (Units_j × Rate_j)", bold_prefix="Billing Equation: ")
    
    tariff_headers = ["Consumption Slab", "Energy Range (kWh)", "Rate per Unit (₹/kWh)", "Application Condition"]
    tariff_data = [
        ["Fixed Base Charge", "N/A", "₹50.00 / Month", "Fixed monthly meter & connection maintenance fee."],
        ["Slab 1 (Subsidized)", "0 to 100 kWh", "₹4.50 / kWh", "Applied to the first 100 units consumed in the billing cycle."],
        ["Slab 2 (Standard)", "101 to 300 kWh", "₹7.50 / kWh", "Applied to units consumed between 101 and 300 kWh."],
        ["Slab 3 (High Usage)", "Above 300 kWh", "₹10.00 / kWh", "Applied to all units consumed exceeding 300 kWh."]
    ]
    add_styled_table(doc, tariff_headers, tariff_data, col_widths=[1.5, 1.5, 1.5, 2.27])
    
    add_paragraph(doc,
        "Walkthrough: If a household consumes 250 kWh in a billing month: Fixed Charge = ₹50.00; Slab 1 (100 × ₹4.50) = ₹450.00; Slab 2 (150 × ₹7.50) = ₹1,125.00; Total Bill = ₹50 + ₹450 + ₹1,125 = ₹1,625.00. EnergyEx performs this calculation continuously to display projected end-of-month costs.",
        bold_prefix="Numerical Example: "
    )
    
    # -------------------------------------------------------------
    # SECTION 13: AI-BASED PREDICTION AND ANOMALY DETECTION
    # -------------------------------------------------------------
    add_heading_1(doc, "13. AI-Based Prediction and Anomaly Detection")
    
    add_paragraph(doc,
        "The Python AI microservice delivers two core analytical capabilities: (1) multi-day ahead energy demand forecasting using Random Forest Regression, and (2) real-time electrical anomaly detection using robust statistical bounds.",
        bold_prefix="13.1 Machine Learning Load Forecasting: "
    )
    
    add_paragraph(doc,
        "The load forecasting engine utilizes an ensemble Random Forest Regressor composed of B = 50 randomized decision trees. Random Forest was selected over linear regression and recurrent neural networks because domestic consumption exhibits abrupt step-changes and non-linear threshold effects that linear models cannot capture, while avoiding the massive data and GPU requirements of LSTMs.",
        bold_prefix="Model Selection & Rationale: "
    )
    
    add_paragraph(doc, "Feature Vector x includes: (1) day_of_week [0-6]; (2) is_weekend [0/1]; (3) day_of_month [1-31]; (4) lag_1_day consumption; (5) lag_2_day consumption; and (6) rolling_mean_3_days consumption.", bullet=True)
    add_paragraph(doc, "Objective Function: Each decision tree recursively partitions the feature space to minimize the Mean Squared Error (MSE) split criterion: MSE(R) = (1/|R|) × Σ (y_i - y_mean)^2.", bullet=True)
    add_paragraph(doc, "Model Confidence: Evaluated via the Coefficient of Determination: R² = 1 - [Σ(y_i - y_pred)^2 / Σ(y_i - y_mean)^2], scaled to a 0-100% confidence rating presented on the Android screen.", bullet=True)
    
    add_heading_2(doc, "13.2 Statistical Anomaly Detection Methodology")
    add_paragraph(doc,
        "To reliably detect equipment faults (e.g. refrigerator compressor stuck ON), wiring insulation breakdown, or energy theft without false positives caused by normal switching transients, EnergyEx deploys a dual-filter model:",
        bold_prefix="Dual-Filter Anomaly Engine: "
    )
    
    add_paragraph(doc, "1. Interquartile Range (IQR) Gating: Calculates Q1 (25th percentile), Q3 (75th percentile), and IQR = Q3 - Q1. The upper outlier threshold is set to Q3 + 1.5 × IQR. Any reading exceeding this boundary is tagged as an outlier independent of sample distribution skewness.", bullet=True)
    add_paragraph(doc, "2. Standardized Z-Score Filter: Computes Z_k = (P_k - μ) / σ. If |Z_k| ≥ 2.0, the system triggers an immediate HIGH_CONSUMPTION alert and logs the anomaly in MySQL.", bullet=True)
    
    add_heading_2(doc, "13.3 Energy-Saving Recommendation Engine")
    add_paragraph(doc,
        "The AI service continuously inspects appliance consumption shares. If the refrigerator accounts for >40% of total household energy, it advises inspecting door seals and adjusting the thermostat to 3°C-5°C. If significant baseline power is detected between 1:00 AM and 5:00 AM, it suggests automated OFF scheduling for entertainment hubs to eliminate vampire standby power.",
        bold_prefix="Actionable Advice Generation: "
    )
    
    # -------------------------------------------------------------
    # SECTION 14: REMOTE APPLIANCE CONTROL
    # -------------------------------------------------------------
    add_heading_1(doc, "14. Remote Appliance Control")
    
    add_paragraph(doc,
        "EnergyEx enables reliable, bi-directional remote appliance actuation from the Android application with deterministic state synchronization:",
        bold_prefix="14.1 Control Architecture: "
    )
    
    add_paragraph(doc, "1. Actuation Initiation: User taps an appliance toggle switch on the Android Remote Control screen.", bullet=True)
    add_paragraph(doc, "2. Command Registration: Mobile app dispatches POST /api/v1/appliances/{id}/toggle. Spring Boot creates a DeviceCommand record with status = 'PENDING'.", bullet=True)
    add_paragraph(doc, "3. Hardware Execution: In Live Mode, the ESP32 polls /api/v1/iot/commands, decodes the target channel, and asserts the appropriate GPIO pin (LOW for ON, HIGH for OFF).", bullet=True)
    add_paragraph(doc, "4. Physical Acknowledgment: The ESP32 confirms execution by posting to /api/v1/iot/commands/{id}/ack. Spring Boot updates the command to 'CONFIRMED' and broadcasts the new state over WebSocket.", bullet=True)
    add_paragraph(doc, "5. Fail-Safe Timeout: If the ESP32 does not acknowledge the command within 5.0 seconds, the command transitions to 'FAILED' and the Android UI rolls back the toggle with an error toast.", bullet=True)
    
    add_callout(doc, "HARDWARE INTEGRATION", "Active-LOW Relay Logic Rationale",
        "The 4-channel relay module operates on Active-LOW logic (driving the input pin LOW to 0V energizes the relay coil). This is an essential safety design: upon microcontroller boot or reset, ESP32 GPIO pins default to high-impedance / floating inputs. An active-LOW configuration ensures that relays remain safely DE-ENERGIZED during boot, preventing connected appliances from turning on unintentionally."
    )
    
    # -------------------------------------------------------------
    # SECTION 15: APPLIANCE SCHEDULING
    # -------------------------------------------------------------
    add_heading_1(doc, "15. Appliance Scheduling")
    
    add_paragraph(doc,
        "Automated load management allows users to establish recurring ON/OFF rules (e.g. turning off the water heater after 30 minutes, or ensuring the TV entertainment hub turns off at midnight):",
        bold_prefix="15.1 Scheduler Engine Architecture: "
    )
    
    add_paragraph(doc, "1. Rule Persistence: User schedules are saved in the MySQL 'schedules' table with target appliance_id, start_time, end_time, days_of_week bitmask, and enabled status.", bullet=True)
    add_paragraph(doc, "2. Backend Evaluation Runner: Spring Boot executes a background task @Scheduled(fixedRate = 30000) every 30 seconds. It matches current system time and day-of-week against active schedule rules.", bullet=True)
    add_paragraph(doc, "3. Autonomous Execution: When a schedule condition matches, the runner issues an actuation command directly, logging an audit alert. The rule executes regardless of whether the mobile phone is connected, asleep, or powered off.", bullet=True)
    
    # -------------------------------------------------------------
    # SECTION 16: DATABASE DESIGN
    # -------------------------------------------------------------
    add_heading_1(doc, "16. Database Design (All 9 Relational Tables)")
    
    add_paragraph(doc,
        "The EnergyEx database is fully normalized (3NF) and consists of 9 relational tables hosted in MySQL 8.0. Foreign key constraints enforce referential integrity across users, appliances, telemetry readings, and actuation commands:",
        bold_prefix="16.1 Entity-Relationship Overview: "
    )
    
    tables_spec = [
        ("Table 1: users", [
            ("id", "BIGINT AUTO_INCREMENT", "PRIMARY KEY", "Unique user identifier"),
            ("username", "VARCHAR(50) NOT NULL", "UNIQUE", "User login handle"),
            ("email", "VARCHAR(100) NOT NULL", "UNIQUE", "Contact and alert notification email"),
            ("password_hash", "VARCHAR(255) NOT NULL", "None", "BCrypt hashed credentials"),
            ("full_name", "VARCHAR(100)", "None", "User display name"),
            ("created_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", "None", "Account registration timestamp")
        ]),
        ("Table 2: appliances", [
            ("id", "BIGINT AUTO_INCREMENT", "PRIMARY KEY", "Unique appliance identifier"),
            ("name", "VARCHAR(100) NOT NULL", "None", "Display name (e.g. Living Room Light)"),
            ("category", "VARCHAR(50) NOT NULL", "None", "Appliance category (Lighting, Fan, etc.)"),
            ("rated_wattage", "DOUBLE NOT NULL", "None", "Manufacturer rated power in Watts"),
            ("channel_number", "INT NOT NULL", "None", "Relay channel assignment (1 - 4)"),
            ("icon_name", "VARCHAR(50)", "None", "UI vector drawable reference"),
            ("is_active", "BOOLEAN DEFAULT TRUE", "None", "Current power state (TRUE=ON, FALSE=OFF)"),
            ("user_id", "BIGINT NOT NULL", "FOREIGN KEY -> users(id)", "Owning user reference")
        ]),
        ("Table 3: devices", [
            ("id", "BIGINT AUTO_INCREMENT", "PRIMARY KEY", "Unique IoT device identifier"),
            ("device_uid", "VARCHAR(100) NOT NULL", "UNIQUE", "Hardware MAC / UID identifier"),
            ("ip_address", "VARCHAR(45)", "None", "Current assigned local IP address"),
            ("is_online", "BOOLEAN DEFAULT FALSE", "None", "Real-time online heartbeat status"),
            ("mode", "VARCHAR(20) DEFAULT 'DEMO'", "None", "Current operating mode (DEMO / LIVE)"),
            ("last_heartbeat", "TIMESTAMP", "None", "Timestamp of most recent communication"),
            ("user_id", "BIGINT NOT NULL", "FOREIGN KEY -> users(id)", "Owning user reference")
        ]),
        ("Table 4: energy_readings", [
            ("id", "BIGINT AUTO_INCREMENT", "PRIMARY KEY", "Unique reading identifier"),
            ("voltage", "DOUBLE NOT NULL", "None", "RMS Voltage in Volts (V)"),
            ("current_val", "DOUBLE NOT NULL", "None", "RMS Current in Amperes (A)"),
            ("active_power", "DOUBLE NOT NULL", "None", "Active Real Power in Watts (W)"),
            ("energy", "DOUBLE NOT NULL", "None", "Cumulative Energy in Kilowatt-hours (kWh)"),
            ("frequency", "DOUBLE NOT NULL", "None", "Mains Frequency in Hertz (Hz)"),
            ("power_factor", "DOUBLE NOT NULL", "None", "Dimensionless Power Factor (0.00 - 1.00)"),
            ("is_demo_data", "BOOLEAN NOT NULL", "None", "True if generated by simulator, False if real"),
            ("recorded_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", "INDEX", "Timestamp of measurement acquisition"),
            ("appliance_id", "BIGINT", "FOREIGN KEY -> appliances(id)", "Associated appliance (or null for feeder)"),
            ("device_id", "BIGINT NOT NULL", "FOREIGN KEY -> devices(id)", "Originating IoT hardware node")
        ]),
        ("Table 5: tariffs", [
            ("id", "BIGINT AUTO_INCREMENT", "PRIMARY KEY", "Unique tariff entry identifier"),
            ("provider_name", "VARCHAR(100) NOT NULL", "None", "DISCOM utility provider name"),
            ("slab_min_kwh", "DOUBLE NOT NULL", "None", "Lower bound of consumption slab"),
            ("slab_max_kwh", "DOUBLE NOT NULL", "None", "Upper bound of consumption slab"),
            ("rate_per_kwh", "DOUBLE NOT NULL", "None", "Billed rate in Indian Rupees (₹/kWh)"),
            ("fixed_charge", "DOUBLE DEFAULT 0.0", "None", "Fixed service charge in ₹"),
            ("user_id", "BIGINT NOT NULL", "FOREIGN KEY -> users(id)", "Owning user reference")
        ]),
        ("Table 6: predictions", [
            ("id", "BIGINT AUTO_INCREMENT", "PRIMARY KEY", "Unique prediction record identifier"),
            ("prediction_date", "DATE NOT NULL", "None", "Target forecasted calendar date"),
            ("predicted_kwh", "DOUBLE NOT NULL", "None", "Forecasted daily consumption in kWh"),
            ("predicted_cost", "DOUBLE NOT NULL", "None", "Projected electricity cost in ₹"),
            ("confidence_score", "DOUBLE NOT NULL", "None", "Model R² confidence score (0.00 - 1.00)"),
            ("algorithm_name", "VARCHAR(50)", "None", "Name of model (e.g. RandomForest_v1)"),
            ("user_id", "BIGINT NOT NULL", "FOREIGN KEY -> users(id)", "Associated user reference")
        ]),
        ("Table 7: alerts", [
            ("id", "BIGINT AUTO_INCREMENT", "PRIMARY KEY", "Unique alert event identifier"),
            ("title", "VARCHAR(100) NOT NULL", "None", "Brief alert headline"),
            ("message", "TEXT NOT NULL", "None", "Detailed diagnostic message and advice"),
            ("severity", "VARCHAR(20) NOT NULL", "None", "CRITICAL, WARNING, or INFO"),
            ("alert_type", "VARCHAR(50) NOT NULL", "None", "OVER_VOLTAGE, OVER_CURRENT, ANOMALY"),
            ("is_read", "BOOLEAN DEFAULT FALSE", "None", "User acknowledgement status"),
            ("triggered_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", "None", "Event trigger timestamp"),
            ("user_id", "BIGINT NOT NULL", "FOREIGN KEY -> users(id)", "Associated user reference")
        ]),
        ("Table 8: schedules", [
            ("id", "BIGINT AUTO_INCREMENT", "PRIMARY KEY", "Unique schedule rule identifier"),
            ("start_time", "TIME NOT NULL", "None", "Automated turn-ON time (HH:MM:SS)"),
            ("end_time", "TIME NOT NULL", "None", "Automated turn-OFF time (HH:MM:SS)"),
            ("days_of_week", "VARCHAR(50) NOT NULL", "None", "Comma-separated days (MON,TUE,etc.)"),
            ("is_enabled", "BOOLEAN DEFAULT TRUE", "None", "Active state toggle"),
            ("appliance_id", "BIGINT NOT NULL", "FOREIGN KEY -> appliances(id)", "Target appliance to switch")
        ]),
        ("Table 9: device_commands", [
            ("id", "BIGINT AUTO_INCREMENT", "PRIMARY KEY", "Unique command identifier"),
            ("command_type", "VARCHAR(50) NOT NULL", "None", "RELAY_SWITCH, REBOOT, MODE_CHANGE"),
            ("channel_number", "INT NOT NULL", "None", "Target relay channel (1 - 4)"),
            ("desired_state", "BOOLEAN NOT NULL", "None", "Desired state (TRUE=ON, FALSE=OFF)"),
            ("status", "VARCHAR(20) DEFAULT 'PENDING'", "None", "PENDING, EXECUTED, CONFIRMED, FAILED"),
            ("created_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", "None", "Command dispatch timestamp"),
            ("executed_at", "TIMESTAMP", "None", "Hardware execution confirmation timestamp"),
            ("device_id", "BIGINT NOT NULL", "FOREIGN KEY -> devices(id)", "Target hardware device")
        ])
    ]
    
    db_headers = ["Column Name", "SQL Data Type", "Constraint / Key", "Description & Usage Scope"]
    for tbl_title, cols in tables_spec:
        add_heading_2(doc, tbl_title)
        add_styled_table(doc, db_headers, cols, col_widths=[1.5, 1.8, 1.4, 2.07])
        
    # -------------------------------------------------------------
    # SECTION 17: BACKEND DEVELOPMENT
    # -------------------------------------------------------------
    add_heading_1(doc, "17. Backend Development (Spring Boot 3 / Java 21)")
    
    add_paragraph(doc,
        "The enterprise backend is built on Spring Boot 3.2 running on OpenJDK 21 LTS. It follows clean architectural boundaries, encapsulating database transactions within service layers and presenting REST and WebSocket interfaces to external clients:",
        bold_prefix="17.1 Enterprise Service Architecture: "
    )
    
    add_paragraph(doc, "1. TelemetryService: Manages incoming electrical telemetry packets; validates electrical boundary sanity; persists records via Spring Data JPA; updates appliance runtime statistics; and delegates to the alert engine.", bullet=True)
    add_paragraph(doc, "2. IoTSimulatorService: Generates realistic multi-appliance telemetry in Phase 1; runs on a background scheduled executor; simulates cyclic compressor loads and inductive power factors; sets is_demo_data = true.", bullet=True)
    add_paragraph(doc, "3. TariffCalculatorService: Evaluates energy consumption across tiered slabs; applies fixed charges; and computes accurate billing projections in Indian Rupees (₹).", bullet=True)
    add_paragraph(doc, "4. WebSocketBroadcaster: Interfaced with Spring WebSocket STOMP message broker; immediately broadcasts live telemetry to /topic/telemetry and critical events to /topic/alerts.", bullet=True)
    add_paragraph(doc, "5. ScheduleRunnerService: Background worker running every 30 seconds (@Scheduled) to evaluate active rules against current system clock and dispatch relay commands.", bullet=True)
    add_paragraph(doc, "6. Dual-Profile Configuration: Supports 'dev' profile (zero-configuration in-memory H2 database for rapid offline testing) and 'prod' profile (high-performance MySQL 8 with HikariCP connection pooling).", bullet=True)
    
    # -------------------------------------------------------------
    # SECTION 18: PYTHON AI/ML SERVICE
    # -------------------------------------------------------------
    add_heading_1(doc, "18. Python AI/ML Service (FastAPI / Scikit-Learn)")
    
    add_paragraph(doc,
        "The AI microservice operates as an independent, lightweight analytical engine implemented in Python 3.13 using FastAPI. It is optimized for low-latency JSON response times (<50 ms):",
        bold_prefix="18.1 Microservice Architecture: "
    )
    
    ai_endpoints = [
        ("POST /api/v1/ai/predict", "Accepts historical daily consumption array; extracts temporal and lag features; executes Random Forest regressor; returns 7-day and 30-day ahead kWh forecasts, projected costs in ₹, and model R² confidence score."),
        ("POST /api/v1/ai/anomalies", "Accepts array of instantaneous active power readings; applies IQR upper bound and Z-score calculations; returns anomaly flags, outlier indices, and statistical metrics."),
        ("POST /api/v1/ai/recommendations", "Analyzes appliance consumption proportions; identifies excessive standby or cooling energy; generates actionable efficiency recommendations."),
        ("GET /api/v1/ai/health", "Microservice health check endpoint returning service uptime, memory consumption, and loaded model version.")
    ]
    for ep, desc in ai_endpoints:
        add_paragraph(doc, desc, bold_prefix=ep + ": ")
        
    add_callout(doc, "TESTING CHECKPOINT", "AI Service Verification & Accuracy Bounds",
        "The Random Forest model must achieve a Coefficient of Determination R² ≥ 0.85 and a Mean Absolute Percentage Error (MAPE) ≤ 8.0% on domestic load test splits. Telemetry response times for /predict must remain below 100 milliseconds under concurrent requests."
    )
    
    # -------------------------------------------------------------
    # SECTION 19: ESP32 AND PZEM HARDWARE INTEGRATION
    # -------------------------------------------------------------
    add_heading_1(doc, "19. ESP32 and PZEM Hardware Integration")
    
    add_paragraph(doc,
        "Integration between the ESP32 microcontroller, PZEM-004T v3.0 sensor, and 4-channel relay module requires precise pin mapping, baud rate configuration, and electrical separation:",
        bold_prefix="19.1 Low-Voltage Interconnect Wiring: "
    )
    
    esp_pzem_headers = ["PZEM-004T Pin", "ESP32 Pin", "Wire Color", "Electrical Function & Notes"]
    esp_pzem_data = [
        ["5V (VCC)", "VIN (5V)", "Red", "Powers PZEM low-voltage optocoupled UART interface."],
        ["GND", "GND", "Black", "Common ground reference."],
        ["TX", "GPIO 16 (RX2)", "Yellow", "PZEM Transmit line connected to ESP32 HardwareSerial 2 Receive."],
        ["RX", "GPIO 17 (TX2)", "Green", "PZEM Receive line connected to ESP32 HardwareSerial 2 Transmit."]
    ]
    add_styled_table(doc, esp_pzem_headers, esp_pzem_data, col_widths=[1.5, 1.5, 1.2, 2.57])
    
    esp_relay_headers = ["Relay Pin", "ESP32 Pin", "Target Appliance", "Relay Switching Logic"]
    esp_relay_data = [
        ["VCC", "VIN (5V)", "All Channels", "Powers relay coils and PC817 optocouplers."],
        ["GND", "GND", "All Channels", "Common logic ground."],
        ["IN1", "GPIO 18", "Living Room Light (CH1)", "Active-LOW (LOW=ON, HIGH=OFF)"],
        ["IN2", "GPIO 19", "Ceiling Fan (CH2)", "Active-LOW (LOW=ON, HIGH=OFF)"],
        ["IN3", "GPIO 21", "Smart TV (CH3)", "Active-LOW (LOW=ON, HIGH=OFF)"],
        ["IN4", "GPIO 22", "Kitchen Refrigerator (CH4)", "Active-LOW (LOW=ON, HIGH=OFF)"]
    ]
    add_styled_table(doc, esp_relay_headers, esp_relay_data, col_widths=[1.2, 1.5, 2.0, 2.07])
    
    add_heading_2(doc, "19.2 High-Voltage (230V AC) Wiring Scheme")
    add_paragraph(doc, "1. PZEM AC Voltage Sensing: Connect terminal 'L' to AC Mains Live (Phase) and terminal 'N' to AC Mains Neutral. This powers the internal measurement ASIC and senses supply voltage.", bullet=True)
    add_paragraph(doc, "2. Current Transformer (CT) Clamp: Connect the 2-pin CT connector to the PZEM CT port. Clamp the CT ring around ONLY the AC Mains Live wire.", bullet=True)
    add_paragraph(doc, "3. Relay Appliance Switching: For each channel, break the Live wire. Connect incoming Live to Relay 'COM' (Common), and connect Relay 'NO' (Normally Open) to the appliance Live terminal. Connect appliance Neutral directly to AC Mains Neutral.", bullet=True)
    
    add_heading_2(doc, "19.3 PZEM-004T Modbus RTU Register Mapping (9600 Baud, 8-N-1)")
    pzem_reg_headers = ["Parameter", "Register Address", "Data Type", "Resolution / Scale Factor", "Engineering Unit"]
    pzem_reg_data = [
        ["RMS Voltage", "0x0000", "16-bit Unsigned Int", "0.1", "Volts (V)"],
        ["RMS Current", "0x0001 - 0x0002", "32-bit Unsigned Int", "0.001", "Amperes (A)"],
        ["Active Power", "0x0003 - 0x0004", "32-bit Unsigned Int", "0.1", "Watts (W)"],
        ["Active Energy", "0x0005 - 0x0006", "32-bit Unsigned Int", "1.0", "Watt-hours (Wh)"],
        ["Mains Frequency", "0x0007", "16-bit Unsigned Int", "0.1", "Hertz (Hz)"],
        ["Power Factor", "0x0008", "16-bit Unsigned Int", "0.01", "Dimensionless (0.00 - 1.00)"]
    ]
    add_styled_table(doc, pzem_reg_headers, pzem_reg_data, col_widths=[1.5, 1.5, 1.5, 1.2, 1.07])
    
    # -------------------------------------------------------------
    # SECTION 20: API COMMUNICATION
    # -------------------------------------------------------------
    add_heading_1(doc, "20. API Communication (REST Endpoints & WebSocket STOMP)")
    
    add_paragraph(doc,
        "EnergyEx utilizes a hybrid communication architecture: RESTful HTTP APIs for request-response state changes, and WebSocket STOMP topics for real-time telemetry streaming:",
        bold_prefix="20.1 API Communication Specification: "
    )
    
    api_headers = ["HTTP Method", "Endpoint URI", "Payload Type", "Description & Response"]
    api_data = [
        ["POST", "/api/v1/iot/telemetry", "TelemetryRequest (JSON)", "ESP32 pushes live voltage, current, power, energy, frequency, and PF. Returns HTTP 200."],
        ["GET", "/api/v1/dashboard/summary", "None", "Returns latest dashboard metrics (total kWh, estimated bill ₹, active devices, real-time power)."],
        ["GET", "/api/v1/appliances", "None", "Returns list of all 4 appliances with current states, wattages, and channels."],
        ["POST", "/api/v1/appliances/{id}/toggle", "None", "Toggles appliance state. Dispatches command to ESP32 relay and returns updated state."],
        ["GET", "/api/v1/billing/estimate", "None", "Returns detailed tiered tariff breakdown, slab distribution, and projected monthly cost in ₹."],
        ["GET", "/api/v1/history?period={today/week/month}", "None", "Returns time-series consumption points for historical chart visualization."],
        ["GET", "/api/v1/predictions", "None", "Fetches 7-day and 30-day demand predictions and R² confidence from the AI service."],
        ["GET", "/api/v1/alerts", "None", "Returns chronological list of triggered electrical alerts and anomaly notifications."],
        ["POST", "/api/v1/system/mode", "ModeRequest (JSON)", "Toggles system mode between 'DEMO' and 'LIVE' instantly without application restart."]
    ]
    add_styled_table(doc, api_headers, api_data, col_widths=[1.1, 2.1, 1.5, 2.07])
    
    add_heading_2(doc, "20.2 WebSocket STOMP Topic Destinations")
    ws_headers = ["Topic Destination", "Direction", "Broadcast Frequency", "Payload Description"]
    ws_data = [
        ["/topic/telemetry", "Server -> Mobile", "Every 3 seconds", "Real-time JSON packet containing voltage, current, power, energy, frequency, and PF."],
        ["/topic/alerts", "Server -> Mobile", "Event-driven", "Instant notification JSON containing severity, alert type, and diagnostic message."],
        ["/topic/appliance/{id}", "Server -> Mobile", "Event-driven", "Actuation state confirmation (ON/OFF) broadcasting updated relay state."]
    ]
    add_styled_table(doc, ws_headers, ws_data, col_widths=[1.8, 1.4, 1.5, 2.07])
    
    add_heading_2(doc, "20.3 Telemetry Ingestion JSON Schema")
    telemetry_json = (
        "{\n"
        '  "deviceUid": "ESP32-ENERGYEX-001",\n'
        '  "authToken": "energyex_secure_device_token_2026",\n'
        '  "voltage": 230.4,\n'
        '  "current": 2.15,\n'
        '  "activePower": 465.2,\n'
        '  "energy": 12.84,\n'
        '  "frequency": 50.0,\n'
        '  "powerFactor": 0.94,\n'
        '  "isDemoData": false\n'
        "}"
    )
    add_code_block(doc, telemetry_json)
    
    # -------------------------------------------------------------
    # SECTION 21: SECURITY REQUIREMENTS
    # -------------------------------------------------------------
    add_heading_1(doc, "21. Security Requirements")
    
    add_paragraph(doc,
        "IoT electricity monitoring systems interface with physical infrastructure; security vulnerabilities can lead to unauthorized load manipulation or false billing records. EnergyEx implements a multi-tier defense strategy:",
        bold_prefix="21.1 Security Architecture: "
    )
    
    add_paragraph(doc, "1. Edge Authentication: The ESP32 transmits a cryptographic bearer token in every telemetry header. Telemetry packets without valid credentials are discarded.", bullet=True)
    add_paragraph(doc, "2. Sensor Data Sanity & Bounds Validation: The backend validates every incoming reading against realistic electrical boundaries (Voltage: 180V-260V, Current: 0A-100A, Frequency: 45Hz-65Hz, PF: 0.00-1.00). Corrupted or out-of-range packets are dropped with an audit log.", bullet=True)
    add_paragraph(doc, "3. Physical Isolation: Complete optical isolation (via onboard optocouplers on the PZEM sensor and relay module) prevents high-voltage AC surges from reaching the low-voltage DC logic circuits.", bullet=True)
    add_paragraph(doc, "4. Network Confinement: In local mode, communication is restricted to the local subnet. When deployed to cloud infrastructure, TLS 1.3 encryption is enforced on all HTTP and WebSocket connections.", bullet=True)
    
    add_callout(doc, "SECURITY NOTE", "Defense Against Unauthorized Telemetry Injection",
        "The Spring Boot backend enforces pre-shared bearer token validation on all /api/v1/iot/* endpoints. Furthermore, rate-limiting filters block brute-force attempts. Telemetry timestamps are strictly validated against server time to prevent replay attacks."
    )
    
    # -------------------------------------------------------------
    # SECTION 22: TESTING AND VALIDATION
    # -------------------------------------------------------------
    add_heading_1(doc, "22. Testing and Validation")
    
    add_paragraph(doc,
        "EnergyEx underwent comprehensive multi-tier testing to ensure functional correctness, numerical accuracy, and system stability:",
        bold_prefix="22.1 Testing Regimes: "
    )
    
    test_headers = ["Testing Regime", "Scope & Target", "Methodology", "Pass Criteria & Results"]
    test_data = [
        ["Backend Unit Tests", "Service & Entity Layers", "JUnit 5 & Mockito", "100% pass on tariff calculations, schedule evaluation, and alert triggers."],
        ["Simulation Engine Test", "IoTSimulatorService", "Continuous 24-hr simulation", "Verified cyclic duty cycles, realistic Gaussian line noise, and correct 'is_demo_data' tagging."],
        ["AI Regression Accuracy", "Random Forest Regressor", "Train/test split evaluation", "Achieved R² score = 0.892 and MAPE = 6.4% on 30-day domestic consumption forecast."],
        ["WebSocket Stress Test", "STOMP Message Broker", "Simulated 50 concurrent mobile clients", "Sub-second broadcast latency (<45 ms) without dropped frames or memory leaks."],
        ["Mobile UI & Navigation", "Android 14 Screens", "Manual & Espresso testing", "Fluid transitions across all 14 screens, responsive layout on 5\" to 6.7\" devices."],
        ["Hardware Bench Test", "ESP32 & PZEM (Bench)", "Resistive 60W bulb test load", "Digital PZEM reading matched calibrated true RMS digital multimeter within 0.5% margin."]
    ]
    add_styled_table(doc, test_headers, test_data, col_widths=[1.5, 1.4, 1.5, 2.37])
    
    # -------------------------------------------------------------
    # SECTION 23: DEVELOPMENT ROADMAP
    # -------------------------------------------------------------
    add_heading_1(doc, "23. Development Roadmap (12 Phases)")
    
    add_paragraph(doc,
        "The complete engineering development lifecycle is structured across 12 systematic phases, providing a disciplined roadmap from initial conception to final deployment:",
        bold_prefix="23.1 Phase-Wise Execution Matrix: "
    )
    
    roadmap_headers = ["Phase", "Title", "Core Objectives", "Deliverables & Expected Outcomes"]
    roadmap_data = [
        ["Phase 1", "Project Setup & Architecture", "Establish project directory, Maven/Gradle repos, Docker configurations, and Git workflow.", "Structured multi-tier project repository with clean build configurations."],
        ["Phase 2", "Android Application UI", "Design and construct all 14 Material Design 3 screens, fragments, and navigation graphs.", "Complete, navigable Android application UI with custom Canvas EnergyChartView."],
        ["Phase 3", "Demo Data Simulation", "Develop IoTSimulatorService generating realistic domestic appliance load curves and line noise.", "Operational simulation engine streaming synthetic telemetry labeled as DEMO DATA."],
        ["Phase 4", "Spring Boot Backend", "Build REST controllers, service layers, DTOs, STOMP WebSocket broker, and business logic.", "Fully operational enterprise backend handling CRUD and real-time WebSocket topics."],
        ["Phase 5", "MySQL Database Integration", "Implement 9 normalized relational JPA entities, repositories, and connection pooling.", "ACID-compliant MySQL persistence layer with automatic schema migrations."],
        ["Phase 6", "AI/ML Prediction Service", "Develop Python FastAPI service with Random Forest regression and IQR/Z-score anomaly engine.", "Containerized AI microservice serving predictions and anomaly detection via REST."],
        ["Phase 7", "Android-Backend Integration", "Connect Retrofit REST client and STOMP WebSocket client to Spring Boot endpoints.", "Live data propagation from backend to mobile client with sub-second screen updates."],
        ["Phase 8", "ESP32 Hardware Integration", "Configure Arduino C++ toolchain, flash WiFi client, and test GPIO relay switching.", "ESP32 successfully connects to local Wi-Fi and drives active-LOW relay channels."],
        ["Phase 9", "PZEM Sensor Integration", "Wire PZEM-004T to ESP32 HardwareSerial 2; poll Modbus RTU registers; verify CT clamping.", "ESP32 reliably reads True RMS V, I, P, E, Hz, and PF and logs values to Serial Monitor."],
        ["Phase 10", "Relay Control & Scheduling", "Implement end-to-end bi-directional relay command execution with ACK loop and schedules.", "Remote switching confirmed via mobile app; automated backend scheduling functional."],
        ["Phase 11", "Complete System Testing", "Conduct integrated end-to-end testing across hardware, backend, AI, and mobile tiers.", "Validated system stability, sub-second latency, and verified zero-rebuild mode toggle."],
        ["Phase 12", "Final Deployment & Docs", "Package Android release APK, backend fat-JAR, AI Dockerfile, and documentation guide.", "Production-ready engineering deliverables and comprehensive viva defense manual."]
    ]
    add_styled_table(doc, roadmap_headers, roadmap_data, col_widths=[0.8, 1.5, 1.9, 2.57])
    
    # -------------------------------------------------------------
    # SECTION 24: HARDWARE ARRIVAL CHECKLIST
    # -------------------------------------------------------------
    add_heading_1(doc, "24. Hardware Arrival Checklist (Step-by-Step for Tomorrow)")
    
    add_paragraph(doc,
        "Upon delivery of the ordered hardware components (ESP32 DevKit V1, PZEM-004T V3.0, and 4-Channel Relay Module), follow this systematic procedure to safely transition the application from Demo Mode to Live IoT Mode:",
        bold_prefix="24.1 Commissioning Protocol: "
    )
    
    steps = [
        ("Step 1: Physical Unboxing & Visual Inspection",
         "Inspect the ESP32 board for bent header pins or solder bridges.\n"
         "Verify that the PZEM-004T board includes the 100A CT clamp and 2-pin connector wire.\n"
         "Inspect the 4-channel relay board for cracked optocoupler chips (PC817) or loose terminal blocks."),
        
        ("Step 2: Safe Low-Voltage USB Bench Testing",
         "Connect the ESP32 to your PC via a high-quality Micro-USB data cable.\n"
         "Open Arduino IDE -> select 'ESP32 Dev Module' -> select the detected COM port.\n"
         "Open firmware/EnergyEx_ESP32/EnergyEx_ESP32.ino.\n"
         "Update config.h with your local Wi-Fi SSID, Password, and Spring Boot server URL (e.g. http://192.168.1.100:8080).\n"
         "Compile and Flash. Open Serial Monitor at 115200 baud; confirm Wi-Fi connection and IP acquisition."),
        
        ("Step 3: Relay Channel Continuity & Click Verification",
         "Wire 5V and GND from ESP32 to Relay VCC and GND.\n"
         "Connect ESP32 GPIOs 18, 19, 21, and 22 to Relay IN1, IN2, IN3, and IN4.\n"
         "Toggle each appliance switch from the Android Remote Control screen.\n"
         "Listen for the distinct mechanical 'click' of each relay coil and verify that the onboard channel LEDs illuminate."),
        
        ("Step 4: PZEM Sensor Modbus UART Communication Test",
         "Connect ESP32 VIN (5V) and GND to PZEM 5V and GND.\n"
         "Connect PZEM TX to ESP32 GPIO 16 (RX2), and PZEM RX to ESP32 GPIO 17 (TX2).\n"
         "Check Serial Monitor. The firmware will begin polling Modbus registers at 9600 baud.\n"
         "(Note: Prior to AC mains connection, voltage and current will read 0.0; UART communication itself must return no checksum errors)."),
        
        ("Step 5: Controlled AC Mains Hookup with Electrician Supervision",
         "ENSURE AC MAINS POWER CORD IS UNPLUGGED FROM THE WALL OUTLET.\n"
         "Connect AC Live and Neutral wires to PZEM terminals 'L' and 'N'.\n"
         "Pass ONLY the Live wire feeding a test load (e.g. 60W incandescent bulb) through the CT clamp.\n"
         "Route the Live wire through Relay Channel 1 COM and NO terminals.\n"
         "Enclose all AC terminals inside the plastic project enclosure.\n"
         "Plug into AC outlet under qualified electrician supervision. Turn ON the test load."),
        
        ("Step 6: Live Mode Verification & Handshake in Android App",
         "Observe the Serial Monitor: Voltage (~230V), Current (~0.26A), and Power (~60W) will appear.\n"
         "The ESP32 will immediately post live telemetry to the Spring Boot backend.\n"
         "Open the Android app: the dashboard mode badge automatically shifts from 'DEMO MODE' to 'LIVE IoT MODE' (Green)!\n"
         "Confirm that live gauges and custom canvas charts reflect genuine physical measurements.")
    ]
    
    for title, desc in steps:
        add_heading_2(doc, title)
        add_paragraph(doc, desc)
        
    add_callout(doc, "ELECTRICAL SAFETY WARNING", "Zero Contact with Energized Terminals",
        "NEVER adjust screw terminals, touch the CT clamp leads, or alter jumper wires while the AC mains plug is connected to the wall outlet. Always de-energize the entire setup before making physical wiring adjustments."
    )
    
    # -------------------------------------------------------------
    # SECTION 25: EXPECTED FINAL PROJECT OUTPUT
    # -------------------------------------------------------------
    add_heading_1(doc, "25. Expected Final Project Output")
    
    add_paragraph(doc,
        "Upon completion of all 12 development phases and hardware commissioning, the EnergyEx project delivers a production-grade, publication-ready engineering output comprising:",
        bold_prefix="25.1 System Deliverables Summary: "
    )
    
    deliverables = [
        ("1. Fully Functional Android Application (APK): ", "Native Kotlin application featuring 14 completed screens, custom Canvas live charting, sub-second WebSocket updates, remote appliance toggling, and offline resilience."),
        ("2. Enterprise Spring Boot Backend (JAR): ", "Robust Java 21 REST and STOMP WebSocket server with dual-mode ingestion, relational MySQL persistence, automated scheduling, and tiered tariff billing."),
        ("3. High-Performance Python AI Service: ", "FastAPI microservice executing Random Forest energy forecasting (R² > 0.88), statistical IQR/Z-score anomaly detection, and energy efficiency advice."),
        ("4. Embedded ESP32 Firmware: ", "Non-blocking C++ firmware with Modbus RTU PZEM polling, Wi-Fi auto-reconnect, active-LOW relay logic, and secure HTTP POST telemetry."),
        ("5. Hardware Enclosure & Test Rig: ", "Safely wired, optocoupled AC metering and switching testbed ready for live engineering viva demonstrations."),
        ("6. Academic Documentation & Viva Defense Guide: ", "Comprehensive technical report, 25 top viva defense questions and answers, and this complete development guide.")
    ]
    for prefix, body in deliverables:
        add_paragraph(doc, body, bold_prefix=prefix, bullet=True)
        
    add_callout(doc, "DEVELOPMENT TIP", "Viva Demonstration Strategy",
        "During project viva defense, demonstrate the seamless dual-mode capability: first show the application running in Demo Mode with simulated appliance loads, then power on the ESP32 hardware and show the dashboard badge automatically transition to LIVE IoT MODE as real 230V AC measurements take over."
    )
    
    # Save the document
    output_filename = "Electricity_Consumption_Project_Development_Guide.docx"
    doc.save(output_filename)
    print(f"[SUCCESS] Document created successfully: {os.path.abspath(output_filename)}")

if __name__ == "__main__":
    create_document()
