@echo off
setlocal
if not defined JAVA_HOME (
    set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
)
if exist target attrib -r target\*.* /s /d >nul 2>&1
"C:\Users\kalir\.m2\wrapper\dists\apache-maven-3.9.16-bin\5grr65jo27hi51sujmtcldfovl\apache-maven-3.9.16\bin\mvn.cmd" %*
