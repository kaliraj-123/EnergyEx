# pyrefly: ignore-errors
# pyright: reportMissingImports=false, reportUnusedImport=false, reportAttributeAccessIssue=false
# type: ignore
"""
EnergyEx AI Service Runner
Launches the FastAPI Machine Learning microservice on port 8000.
"""

import os
import sys
import importlib

# Add current directory and user site-packages to path
_base_dir = os.path.dirname(os.path.abspath(__file__))
if _base_dir not in sys.path:
    sys.path.insert(0, _base_dir)

user_site = r"C:\Users\kalir\AppData\Roaming\Python\Python313\site-packages"
if os.path.exists(user_site) and user_site not in sys.path:
    sys.path.insert(0, user_site)

if __name__ == "__main__":
    uvicorn = importlib.import_module("uvicorn")
    print("=" * 60)
    print("  EnergyEx AI Machine Learning Microservice")
    print("  Serving Random Forest Regressor & Anomaly Detection")
    print("  Host: http://localhost:8000  |  Docs: http://localhost:8000/docs")
    print("=" * 60)
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=False)
