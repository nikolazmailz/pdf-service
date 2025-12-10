<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8"/>
    <style>
        @page {
            size: A4;
            margin: 0;
        }

        body {
            margin: 0;
            padding: 0;
            font-family: "DejaVu Sans", sans-serif;
            font-size: 10pt;
            color: #0057AE;
        }

        .stamp-container {
            border: 1px solid #0057AE;
            margin: 20pt;
            padding-top: 6pt;
            padding-bottom: 6pt;
        }

        .stamp-header {
            text-align: center;
            font-weight: bold;
            margin-bottom: 6pt;
        }

        .stamp-table {
            width: 100%;
            border-collapse: collapse;
        }

        .stamp-table th,
        .stamp-table td {
            padding: 4pt 6pt;
            vertical-align: top;
        }

        .stamp-table thead th {
            font-weight: bold;
            border-top: 1px solid #0057AE;
        }

        .stamp-table .subheader {
            font-size: 8pt;
            font-weight: normal;
        }

        .row-divider td {
            border-top: 1px solid #0057AE;
            padding-top: 6pt;
        }

        .signer-col {
            width: 28%;
        }

        .cert-col {
            width: 47%;
        }

        .date-col {
            width: 25%;
            white-space: nowrap;
        }

        .line {
            display: block;
        }
    </style>
</head>
<body>
<div class="stamp-container">
    <div class="stamp-header">
        Документ ${documentId} подписан в системе ${systemName}
    </div>

    <table class="stamp-table">
        <thead>
        <tr>
            <th class="signer-col">
                Подписант<br/>
                <span class="subheader">(ЮЛ, Должность, ФИО)</span>
            </th>
            <th class="cert-col">
                Сертификат<br/>
                <span class="subheader">(тип, кем выдан, идентификатор, период действия)</span>
            </th>
            <th class="date-col">
                Дата и время подписания
            </th>
        </tr>
        </thead>
        <tbody>
        <#list signers as signer>
            <tr class="row-divider">
                <td>
                    <#list signer.signerBlockLines as line>
                        <span class="line">${line}</span>
                    </#list>
                </td>
                <td>
                    <#list signer.certificateLines as line>
                        <span class="line">${line}</span>
                    </#list>
                </td>
                <td class="date-col">
                    <#list signer.signingTimeLines as line>
                        <span class="line">${line}</span>
                    </#list>
                </td>
            </tr>
        </#list>
        </tbody>
    </table>
</div>
</body>
</html>
