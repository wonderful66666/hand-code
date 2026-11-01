$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'environment.ps1')
Push-Location $ProjectRoot
try {
    Invoke-Checked -Command mvn -CommandArguments @('-B', '-ntp', 'verify')
    Invoke-Checked -Command cargo -CommandArguments @('fmt', '--manifest-path', 'WithYourHandRust/Cargo.toml', '--check')
    Invoke-Checked -Command cargo -CommandArguments @('clippy', '--manifest-path', 'WithYourHandRust/Cargo.toml', '--locked', '--all-targets', '--', '-D', 'warnings')
    Invoke-Checked -Command cargo -CommandArguments @('test', '--manifest-path', 'WithYourHandRust/Cargo.toml', '--locked')
    Invoke-Checked -Command npm -CommandArguments @('run', 'lint', '--prefix', 'frontend')
    Invoke-Checked -Command npm -CommandArguments @('run', 'build', '--prefix', 'frontend')
    Write-Host 'All checks passed.'
} finally {
    Pop-Location
}
