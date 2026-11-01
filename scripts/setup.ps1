$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'environment.ps1')
Push-Location $ProjectRoot
try {
    $javaVersion = (& javac -version 2>&1 | Out-String).Trim()
    $rustVersion = (& rustc --version | Out-String).Trim()
    if ($javaVersion -notmatch '^javac 25\.0\.1$') {
        throw "Expected the project JDK 25.0.1, found: $javaVersion. Check JAVA_HOME and PATH."
    }
    if ($rustVersion -notmatch '^rustc 1\.93\.0 ') {
        throw "Expected Rust 1.93.0, found: $rustVersion. Reuse the installed toolchain."
    }
    Invoke-Checked -Command npm -CommandArguments @('ci', '--prefix', 'frontend')
    Invoke-Checked -Command mvn -CommandArguments @('-B', '-ntp', 'verify')
    Invoke-Checked -Command cargo -CommandArguments @('fetch', '--manifest-path', 'WithYourHandRust/Cargo.toml', '--locked')
    Write-Host 'Ready. Run scripts/dev.ps1 frontend, qbh, ymy or rust in separate terminals.'
} finally {
    Pop-Location
}
