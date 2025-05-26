Start-Process powershell {.\run.ps1 monitor}
Start-Process powershell {.\run.ps1 broker}
Start-Sleep -Seconds 2
Start-Process powershell {.\run.ps1 server one}
Start-Sleep -Seconds 2
Start-Process powershell {.\run.ps1 server two}
Start-Sleep -Seconds 2
Start-Process powershell {.\run.ps1 client}