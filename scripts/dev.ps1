param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('frontend', 'qbh', 'ymy', 'rust')]
    [string]$Service
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'environment.ps1')
Push-Location $ProjectRoot
try {
    switch ($Service) {
        'frontend' { Invoke-Checked -Command npm -CommandArguments @('run', 'dev', '--prefix', 'frontend') }
        'qbh' { Invoke-Checked -Command mvn -CommandArguments @('-f', 'WithYourHandQBH/pom.xml', 'spring-boot:run') }
        'ymy' { Invoke-Checked -Command mvn -CommandArguments @('-f', 'WithYourHandYmy/pom.xml', 'spring-boot:run') }
        'rust' { Invoke-Checked -Command cargo -CommandArguments @('run', '--manifest-path', 'WithYourHandRust/Cargo.toml', '--locked') }
    }
} finally {
    Pop-Location
}
