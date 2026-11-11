# pyrefly: ignore-errors
# pyright: reportMissingImports=false, reportMissingModuleSource=false, reportAttributeAccessIssue=false
# type: ignore
"""
EnergyEx Word Document Generator
Generates a publication-grade, professionally formatted Microsoft Word document (.docx):
'Electricity_Consumption_Project_Development_Guide.docx'
"""

import os
import sys
import docx
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import parse_xml, OxmlElement
from docx.oxml.ns import nsdecls, qn

# Color Palette
HEX_PRIMARY_NAVY = "003366"      # Deep Academic Navy
HEX_SECONDARY_TEAL = "008080"    # Professional Teal
HEX_DARK_SLATE = "1E293B"        # Charcoal Slate for Headings
HEX_BODY_TEXT = "334155"         # Slate Grey Body Text
HEX_MUTED_GREY = "64748B"        # Captions & Footers
HEX_BORDER_GREY = "CBD5E1"       # Subtle Table Borders
HEX_ZEBRA_BG = "F8FAFC"          # Table Row Alternating Shading

# Callout Colors
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
    
    # Title paragraph
    p_title = cell.paragraphs[0]
    p_title.paragraph_format.space_before = Pt(2)
    p_title.paragraph_format.space_after = Pt(4)
    p_title.paragraph_format.line_spacing = 1.15
    run_icon = p_title.add_run(f"{config['icon']}  {title.upper()}\n")
    run_icon.bold = True
    run_icon.font.name = "Calibri"
    run_icon.font.size = Pt(11)
    run_icon.font.color.rgb = config["title_color"]
    
    # Text paragraph
    p_text = cell.add_paragraph()
    p_text.paragraph_format.space_before = Pt(0)
    p_text.paragraph_format.space_after = Pt(2)
    p_text.paragraph_format.line_spacing = 1.15
    p_text.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    run_text = p_text.add_run(text)
    run_text.font.name = "Calibri"
    run_text.font.size = Pt(10)
    run_text.font.color.rgb = COLOR_SLATE
    
    # Spacer after table
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
    
    # Header Row
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
        
    # Data Rows
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

print("Base layout engine compiled successfully.")

