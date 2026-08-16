-- seed data for regression_cases (историческое имя таблицы: test_messages_from_25)
INSERT INTO test_messages_from_25 (test_cod, test_description, auth, clr) VALUES
  ('test_CASH_original_auth', 'CASH original auth', $jtestCASHoriginalauthauth${
    "rId": "a4db97e8-7b13-4d14-8061-a3bd4fabb2c9",
    "userAgent": "RadixWare",
    "systemCode": "2218",
    "msName": "m263",
    "ip": "10.234.26.129",
    "cbiRequest": {
        "holdActions": {
            "action": [
                {
                    "accountClientId": 3296563,
                    "accountPlanCode": "004",
                    "origAmtDelta": 20500,
                    "amtDelta": 20500,
                    "accountContractRid": "fc_019fb40d-25e0-7143-8fb2-e3974b49c9a0",
                    "origCcy": 643,
                    "accountNumber": "fc_019fb40d-25e0-7143-8fb2-e3974b49c9a0",
                    "holdKind": "Auth",
                    "accountId": 2857897,
                    "accountCcy": 643,
                    "accountClientRid": "ul_1000219616578",
                    "accountContractId": 7449461,
                    "actionKind": 1,
                    "accountPlanSubCode": "001",
                    "accountPlanGuid": "DGY5YENQEFH7RAKMQEKKRLOFDQ",
                    "holdId": 29598832,
                    "holdSign": -1
                }
            ]
        },
        "tranRequest": {
            "initiatorRid": "RTPAUTH",
            "userAttrs": {
                "mode": "SYNC",
                "seq": 0,
                "paramValue": [
                    {
                        "val": "W4",
                        "rid": "MK_Inc_Channel"
                    },
                    {
                        "val": "01",
                        "rid": "MK_Inc_ProcCode"
                    },
                    {
                        "val": "100",
                        "rid": "MK_Inc_Type"
                    },
                    {
                        "val": "2",
                        "rid": "W4_25"
                    },
                    {
                        "val": "0",
                        "rid": "MK_Iss_Transitive"
                    },
                    {
                        "val": "622195093448",
                        "rid": "extRrn"
                    },
                    {
                        "val": "YES",
                        "title": "CERTINFO",
                        "rid": "CERT"
                    }
                ]
            },
            "undoState": "Normal",
            "kind": "Cash",
            "link": [],
            "match": {
                "acquirerRid": "888884",
                "storeToDoer": [],
                "checkForDuplicate": false,
                "linkageKind": "Normal",
                "key": "C2C79672014882477C66D2192ABBB755DDB60FE7260809222107130589",
                "rrn": "260809222107130589"
            },
            "rollbackOnAnyResult": false,
            "isAdvice": false,
            "refineRs": [],
            "isReversal": false,
            "specific": {
                "setupRestriction": [],
                "deferred": false,
                "fastFunds": false,
                "contractLink": [],
                "receiptRequested": false,
                "atcUpdate": false,
                "prepurchase": false
            },
            "networkSpecific": {
                "way4": {
                    "w447912": "0",
                    "w447958": "901100010"
                },
                "visa": {},
                "inactiveReleaseFeatures": []
            },
            "version": "3.2.42.10.19",
            "localTime": "2026-08-09T09:10:10.000",
            "originatorUnitId": 63,
            "originatorDay": "2026-08-10T00:00:00.000",
            "rollbackOnResults": [],
            "isMigration": false,
            "parties": {
                "term": {
                    "owner": {
                        "zip": "108813",
                        "country": 643,
                        "streetAddress": "D. 33, UL. RADUZHNAYA",
                        "city": "g. Moskovskii",
                        "ccyResidenceCountries": [],
                        "rid": "VB24",
                        "mcc": 6011
                    },
                    "total": [],
                    "acquirerRid": "888884",
                    "rid": "308852",
                    "type": "Atm",
                    "acquirerCountry": 643,
                    "channelEncryption": false,
                    "usePaymentTransitAcct": false,
                    "caps": {
                        "signAnalysis": false,
                        "dsrp": false,
                        "tds": false,
                        "contactless": true,
                        "icc": true,
                        "mobile": false,
                        "interactive": false,
                        "cardCapture": true,
                        "barCode": false,
                        "singleTap": false,
                        "magWrite": false,
                        "pin": true,
                        "mpos": false,
                        "magRead": true,
                        "maxPinLen": 6,
                        "partialApproval": false,
                        "keyEntry": true,
                        "attendance": true,
                        "ocr": false
                    }
                },
                "cust": {
                    "auth": {
                        "photoChecked": false,
                        "regTrustedMerchant": false,
                        "biometricsChecked": false,
                        "signChecked": false,
                        "personIdChecked": false
                    },
                    "contractRid": "fc_019fb40d-25e0-7143-8fb2-e3974b49c9a0",
                    "person": {
                        "documents": {
                            "document": []
                        },
                        "ccyResidenceCountries": []
                    },
                    "presence": true,
                    "token": [
                        {
                            "kind": "Card",
                            "isoAccountType": 20,
                            "card": {
                                "auth": {
                                    "pinBlock": "MA==",
                                    "credentialCapturedForResults": [],
                                    "tdsChecked": false,
                                    "signChecked": false,
                                    "presence": true
                                },
                                "serviceCode": "201",
                                "plasticId": "5b37dfcb-c64c-4c20-8922-20a1534d0a28",
                                "cardId": "6f537799-72a9-4fd4-a299-a02d19b41fe7",
                                "expDate": "2028-01-01T00:00:00.000",
                                "emv": {
                                    "mbr": 90
                                },
                                "entryMode": "IccContactless"
                            }
                        }
                    ]
                }
            },
            "lifePhase": "Auth",
            "isPartial": false,
            "moneys": {
                "clear": {
                    "convRate": 1,
                    "ccy": 643,
                    "amt": 20500,
                    "convDate": "2026-08-09T00:00:00.000"
                },
                "cust": {
                    "ccy": 643,
                    "amt": 20500
                }
            },
            "preprocessOnly": false,
            "isBackward": false
        }
    },
    "lmdAttrs": {
        "system": "CCOP",
        "version": 1,
        "createTime": "2026-08-09T09:10:10.000+03:00",
        "updateTime": "2026-08-09T09:10:10.898+03:00"
    },
    "status": {
        "operationStatus": "New"
    }
}$jtestCASHoriginalauthauth$, $jtestCASHoriginalauthclr${
    "systemCode": "2218",
    "msName": "m095",
    "cbiRequest": {
        "tranRequest": {
            "kind": "Cash",
            "lifePhase": "Presentment",
            "localTime": "2026-08-09T09:10:10.000",
            "undoState": "Normal",
            "isReversal": false,
            "isPartial": false,
            "isAdvice": true,
            "parties": {
                "term": {
                    "rid": "308852",
                    "type": "Atm",
                    "owner": {
                        "country": 643,
                        "city": "MOSKOVSKII G",
                        "mcc": 6011,
                        "title": "D. 33, UL. RADUZHNAYA",
                        "zip": "00000",
                        "rid": "VB24"
                    },
                    "caps": {
                        "icc": true,
                        "keyEntry": false,
                        "contactless": false,
                        "magRead": true,
                        "ocr": false,
                        "barCode": false,
                        "pin": true,
                        "maxPinLen": 6,
                        "signAnalysis": false,
                        "cardCapture": true,
                        "attendance": true,
                        "locationKind": "BranchIndoor",
                        "mpos": false,
                        "mobile": false
                    },
                    "acquirerRid": "888884"
                },
                "cust": {
                    "presence": false,
                    "auth": {
                        "signChecked": false
                    },
                    "token": [
                        {
                            "card": {
                                "entryMode": "IccContactless",
                                "expDate": "2028-01-01T00:00:00.000",
                                "cardId": "6f537799-72a9-4fd4-a299-a02d19b41fe7"
                            },
                            "kind": "Card"
                        }
                    ]
                }
            },
            "match": {
                "key": "019fe62e-0386-7727-9ab5-29b428f2a6a4"
            },
            "link": [
                {
                    "key": "C2C79672014882477C66D2192ABBB755DDB60FE7260809222107130589"
                }
            ],
            "moneys": {
                "clear": {
                    "amt": 20500.0,
                    "ccy": 643
                },
                "cust": {
                    "amt": 20500.0,
                    "ccy": 643
                }
            },
            "specific": {
                "multiclearing": false,
                "isFinalPayment": false
            },
            "userAttrs": {
                "paramValue": [
                    {
                        "val": "622195093448",
                        "rid": "extRrn"
                    },
                    {
                        "val": "78888846221043697694711",
                        "rid": "ARN"
                    },
                    {
                        "val": "W4",
                        "rid": "MK_Inc_Channel"
                    },
                    {
                        "val": "070",
                        "rid": "MK_Inc_ProcCode"
                    }
                ]
            },
            "approvalCode": "M9SKEG"
        }
    },
    "lmdAttrs": {
        "system": "CCOP",
        "version": 1,
        "createTime": "2026-08-09T14:00:19.369516+03:00",
        "updateTime": "2026-08-09T14:00:19.372660813+03:00"
    },
    "status": {
        "operationStatus": "Received"
    },
    "rId": "019fe62e-0386-7727-9ab5-29b428f2a6a4"
}$jtestCASHoriginalauthclr$),
  ('test_DEPOSIT_original_auth', 'DEPOSIT original auth', $jtestDEPOSIToriginalauthauth${
    "rId": "98da079a-7d4b-4460-91dd-b13e04ac996e",
    "userAgent": "RadixWare",
    "systemCode": "2218",
    "msName": "m263",
    "ip": "10.234.26.129",
    "cbiRequest": {
        "holdActions": {
            "action": [
                {
                    "accountClientId": 3218385,
                    "accountPlanCode": "004",
                    "origAmtDelta": -390000,
                    "amtDelta": -390000,
                    "accountContractRid": "fc_6db41985-26d7-42d0-a38f-0d3ef00f0113",
                    "origCcy": 643,
                    "accountNumber": "fc_6db41985-26d7-42d0-a38f-0d3ef00f0113",
                    "holdKind": "Auth",
                    "accountId": 2811883,
                    "accountCcy": 643,
                    "accountClientRid": "ul_1000007687023",
                    "accountContractId": 7352828,
                    "actionKind": 1,
                    "accountPlanSubCode": "001",
                    "accountPlanGuid": "DGY5YENQEFH7RAKMQEKKRLOFDQ",
                    "holdId": 29091984,
                    "holdSign": -1
                }
            ]
        },
        "tranRequest": {
            "initiatorRid": "RTPAUTH",
            "userAttrs": {
                "mode": "SYNC",
                "seq": 0,
                "paramValue": [
                    {
                        "val": "W4",
                        "rid": "MK_Inc_Channel"
                    },
                    {
                        "val": "29",
                        "rid": "MK_Inc_ProcCode"
                    },
                    {
                        "val": "220",
                        "rid": "MK_Inc_Type"
                    },
                    {
                        "val": "2",
                        "rid": "W4_25"
                    },
                    {
                        "val": "KVHRH2",
                        "rid": "W4_38"
                    },
                    {
                        "val": "0",
                        "rid": "MK_Iss_Transitive"
                    },
                    {
                        "val": "621490889640",
                        "rid": "extRrn"
                    },
                    {
                        "val": "YES",
                        "title": "CERTINFO",
                        "rid": "CERT"
                    }
                ]
            },
            "approvalCode": "KVHRH2",
            "link": [
                {
                    "approvalCode": "KVHRH2",
                    "checkPrevTranOutLinkKinds": [],
                    "search2Dept": 1,
                    "kind": "Preprocess2Process",
                    "prev": {
                        "auth": {
                            "pinPresence": true
                        }
                    },
                    "alternativeForLinkKinds": [],
                    "rrn": "260802390435598974",
                    "allowedLifePhases": [],
                    "search1Dept": 1,
                    "resultOnFailure": "Approved",
                    "acquirerRid": "888884",
                    "key": "056B40912B580917E70D9567A25BCF75C68BC338260802390435598974",
                    "allowedTranKinds": []
                }
            ],
            "rollbackOnAnyResult": false,
            "result": "Approved",
            "originatorUnitId": 41,
            "originatorDay": "2026-08-03T00:00:00.000",
            "isPartial": false,
            "isBackward": false,
            "undoState": "Normal",
            "kind": "Deposit",
            "match": {
                "acquirerRid": "888884",
                "storeToDoer": [],
                "checkForDuplicate": false,
                "linkageKind": "Normal",
                "key": "056B40912B580917E70D9567A25BCF75C68BC338260802390625398999",
                "rrn": "260802390625398999"
            },
            "isAdvice": true,
            "refineRs": [],
            "isReversal": false,
            "specific": {
                "setupRestriction": [],
                "deferred": false,
                "fastFunds": true,
                "contractLink": [],
                "receiptRequested": false,
                "atcUpdate": false,
                "prepurchase": false
            },
            "networkSpecific": {
                "way4": {
                    "w447912": "0",
                    "w447958": "901100010"
                },
                "visa": {},
                "inactiveReleaseFeatures": []
            },
            "version": "3.2.42.10.19",
            "localTime": "2026-08-02T13:50:43.000",
            "rollbackOnResults": [],
            "isMigration": false,
            "parties": {
                "term": {
                    "owner": {
                        "zip": "620050",
                        "country": 643,
                        "streetAddress": "STR. 63, UL. TEHNICHESKAY",
                        "city": "g. Ekaterinbu",
                        "ccyResidenceCountries": [],
                        "rid": "VB24",
                        "mcc": 6011,
                        "title": "Bank VTB (PAO)"
                    },
                    "total": [],
                    "acquirerRid": "888884",
                    "rid": "302880",
                    "type": "Atm",
                    "acquirerCountry": 643,
                    "channelEncryption": false,
                    "usePaymentTransitAcct": false,
                    "caps": {
                        "signAnalysis": false,
                        "dsrp": false,
                        "tds": false,
                        "contactless": true,
                        "icc": true,
                        "mobile": false,
                        "interactive": false,
                        "cardCapture": true,
                        "barCode": false,
                        "singleTap": false,
                        "magWrite": false,
                        "pin": true,
                        "mpos": false,
                        "magRead": true,
                        "maxPinLen": 6,
                        "partialApproval": false,
                        "keyEntry": true,
                        "attendance": true,
                        "ocr": false
                    }
                },
                "cust": {
                    "auth": {
                        "photoChecked": false,
                        "regTrustedMerchant": false,
                        "biometricsChecked": false,
                        "signChecked": false,
                        "personIdChecked": false
                    },
                    "contractRid": "fc_6db41985-26d7-42d0-a38f-0d3ef00f0113",
                    "person": {
                        "documents": {
                            "document": []
                        },
                        "ccyResidenceCountries": []
                    },
                    "presence": true,
                    "token": [
                        {
                            "kind": "Card",
                            "card": {
                                "auth": {
                                    "credentialCapturedForResults": [],
                                    "tdsChecked": false,
                                    "signChecked": false,
                                    "presence": true
                                },
                                "plasticId": "939ed329-dfe2-464b-834f-b406d0a7519e",
                                "cardId": "1a7f67de-46bb-4070-9b5c-a5c314e5c808",
                                "panCrypt": {
                                    "data": "2D3D94E7D0C46DEC7FFDE02B49BB0AF721CDA3447D45862B",
                                    "keyId": 12
                                },
                                "expDate": "2029-05-01T00:00:00.000",
                                "emv": {
                                    "mbr": 90
                                },
                                "entryMode": "IccContactless"
                            }
                        }
                    ]
                }
            },
            "lifePhase": "Auth",
            "moneys": {
                "clear": {
                    "convRate": 1,
                    "ccy": 643,
                    "amt": 390000,
                    "convDate": "2026-08-02T00:00:00.000"
                },
                "cust": {
                    "ccy": 643,
                    "amt": 390000
                }
            },
            "preprocessOnly": false
        }
    },
    "lmdAttrs": {
        "system": "CCOP",
        "version": 1,
        "createTime": "2026-08-02T13:50:43.000+03:00",
        "updateTime": "2026-08-02T13:51:14.749+03:00"
    },
    "status": {
        "operationStatus": "New"
    }
}$jtestDEPOSIToriginalauthauth$, $jtestDEPOSIToriginalauthclr${
    "systemCode": "2218",
    "msName": "m095",
    "cbiRequest": {
        "tranRequest": {
            "kind": "Deposit",
            "lifePhase": "Presentment",
            "localTime": "2026-08-02T13:51:02.000",
            "undoState": "Normal",
            "isReversal": false,
            "isPartial": false,
            "isAdvice": true,
            "parties": {
                "term": {
                    "rid": "302880",
                    "type": "Atm",
                    "owner": {
                        "country": 643,
                        "city": "EKATERINBURG",
                        "mcc": 6011,
                        "title": "VB24",
                        "zip": "00000",
                        "rid": "VB24"
                    },
                    "caps": {
                        "icc": true,
                        "keyEntry": false,
                        "contactless": false,
                        "magRead": true,
                        "ocr": false,
                        "barCode": false,
                        "pin": true,
                        "maxPinLen": 6,
                        "signAnalysis": false,
                        "cardCapture": true,
                        "attendance": true,
                        "locationKind": "BranchIndoor",
                        "mpos": false,
                        "mobile": false
                    },
                    "acquirerRid": "888884"
                },
                "cust": {
                    "presence": false,
                    "auth": {
                        "signChecked": false
                    },
                    "token": [
                        {
                            "card": {
                                "entryMode": "IccContactless",
                                "expDate": "2029-05-01T00:00:00.000",
                                "cardId": "1a7f67de-46bb-4070-9b5c-a5c314e5c808"
                            },
                            "kind": "Card"
                        }
                    ]
                }
            },
            "match": {
                "key": "019fc2c6-4756-7703-ab30-5a8d194a5319"
            },
            "link": [
                {
                    "key": "056B40912B580917E70D9567A25BCF75C68BC338260802390625398999"
                }
            ],
            "moneys": {
                "clear": {
                    "amt": 390000.0,
                    "ccy": 643
                },
                "cust": {
                    "amt": 390000.0,
                    "ccy": 643
                }
            },
            "specific": {
                "multiclearing": false,
                "isFinalPayment": false
            },
            "userAttrs": {
                "paramValue": [
                    {
                        "val": "621490889640",
                        "rid": "extRrn"
                    },
                    {
                        "val": "78888846214043678280737",
                        "rid": "ARN"
                    },
                    {
                        "val": "W4",
                        "rid": "MK_Inc_Channel"
                    },
                    {
                        "val": "062",
                        "rid": "MK_Inc_ProcCode"
                    }
                ]
            },
            "approvalCode": "KVHRH2"
        }
    },
    "lmdAttrs": {
        "system": "CCOP",
        "version": 1,
        "createTime": "2026-08-02T17:00:08.762458+03:00",
        "updateTime": "2026-08-02T17:00:08.764382867+03:00"
    },
    "status": {
        "operationStatus": "Received"
    },
    "rId": "019fc2c6-4756-7703-ab30-5a8d194a5319"
}$jtestDEPOSIToriginalauthclr$),
  ('test_GOODS_WITH_CASHBACK_original_auth', 'GOODS WITH CASHBACK original auth', $jtestGOODSWITHCASHBACKoriginalauthauth${
    "rId": "ef5a2df8-2bec-4fa7-bece-8a9fce04bc09",
    "userAgent": "RadixWare",
    "systemCode": "2218",
    "msName": "m263",
    "ip": "10.234.26.129",
    "cbiRequest": {
        "holdActions": {
            "action": [
                {
                    "accountClientId": 2554311,
                    "accountPlanCode": "004",
                    "origAmtDelta": 3024.98,
                    "amtDelta": 3024.98,
                    "accountContractRid": "fc_f7e488dc-a254-4841-8ce6-1cf1e1cdd30f",
                    "origCcy": 643,
                    "accountNumber": "fc_f7e488dc-a254-4841-8ce6-1cf1e1cdd30f",
                    "holdKind": "Auth",
                    "accountId": 2441161,
                    "accountCcy": 643,
                    "accountClientRid": "ul_1000188481698",
                    "accountContractId": 6576062,
                    "actionKind": 1,
                    "accountPlanSubCode": "001",
                    "accountPlanGuid": "DGY5YENQEFH7RAKMQEKKRLOFDQ",
                    "holdId": 29622521,
                    "holdSign": -1
                }
            ]
        },
        "tranRequest": {
            "initiatorRid": "RTPAUTH",
            "userAttrs": {
                "mode": "SYNC",
                "seq": 0,
                "paramValue": [
                    {
                        "val": "MIR",
                        "rid": "MK_Inc_Channel"
                    },
                    {
                        "val": "09",
                        "rid": "MK_Inc_ProcCode"
                    },
                    {
                        "val": "100",
                        "rid": "MK_Inc_Type"
                    },
                    {
                        "val": "071",
                        "rid": "MK_Inc_F22"
                    },
                    {
                        "val": "0",
                        "rid": "MK_Iss_Transitive"
                    },
                    {
                        "val": "622155781364",
                        "rid": "extRrn"
                    },
                    {
                        "val": "YES",
                        "title": "CERTINFO",
                        "rid": "CERT"
                    }
                ]
            },
            "undoState": "Normal",
            "kind": "GoodsWithCashback",
            "link": [],
            "match": {
                "acquirerRid": "70372200101",
                "storeToDoer": [],
                "checkForDuplicate": false,
                "linkageKind": "Normal",
                "nrn": "6221674253806495",
                "key": "F2E64E5137A17D3DBB8BD16D9A75A7C7250D3EA6260809430984961823",
                "rrn": "260809430984961823"
            },
            "rollbackOnAnyResult": false,
            "isAdvice": false,
            "refineRs": [],
            "isReversal": false,
            "networkSpecific": {
                "nspk": {
                    "trn": 6221674253806495,
                    "messageType": 100,
                    "posData": "001100030603095    ",
                    "tranFraudIndicator": "0000000000000000220000000000000000000000",
                    "actualEci": "0",
                    "cardProductId": "PPB",
                    "posEntryMode": "071",
                    "networkIdentifier": "0001"
                },
                "inactiveReleaseFeatures": []
            },
            "version": "3.2.42.10.19",
            "localTime": "2026-08-09T14:58:11.000",
            "originatorUnitId": 32,
            "originatorDay": "2026-08-10T00:00:00.000",
            "rollbackOnResults": [],
            "isMigration": false,
            "parties": {
                "term": {
                    "owner": {
                        "zip": "603095",
                        "country": 643,
                        "documents": {
                            "document": [
                                {
                                    "kind": "TaxNumber",
                                    "value": "7825706086"
                                }
                            ]
                        },
                        "city": "N.NOVGOROD",
                        "ccyResidenceCountries": [],
                        "rid": "990000016859",
                        "mcc": 5411,
                        "title": "PYATEROCHKA 2595"
                    },
                    "total": [],
                    "acquirerRid": "70372200101",
                    "rid": "10366242",
                    "type": "Pos",
                    "channelEncryption": false,
                    "usePaymentTransitAcct": false,
                    "forwarderRid": "70372200101",
                    "caps": {
                        "signAnalysis": false,
                        "dsrp": false,
                        "contactless": true,
                        "goodsAuthOnly": false,
                        "icc": false,
                        "mobile": false,
                        "cardCapture": true,
                        "locationKind": "BranchIndoor",
                        "barCode": false,
                        "singleTap": false,
                        "magWrite": false,
                        "pin": true,
                        "mpos": false,
                        "magRead": false,
                        "maxPinLen": 6,
                        "keyEntry": false,
                        "attendance": true,
                        "ocr": false
                    }
                },
                "cust": {
                    "auth": {
                        "photoChecked": false,
                        "regTrustedMerchant": false,
                        "biometricsChecked": false,
                        "signChecked": false,
                        "personIdChecked": false
                    },
                    "contractRid": "fc_f7e488dc-a254-4841-8ce6-1cf1e1cdd30f",
                    "presence": true,
                    "token": [
                        {
                            "kind": "Card",
                            "card": {
                                "auth": {
                                    "pinBlock": "MA==",
                                    "credentialCapturedForResults": [],
                                    "presence": true
                                },
                                "serviceCode": "206",
                                "plasticId": "7b5bbb3b-87ea-45ec-b377-8a6002a0f4e6",
                                "cardId": "87159b64-5447-4785-a419-719e39ce9426",
                                "expDate": "2029-04-01T00:00:00.000",
                                "emv": {
                                    "mbr": 90
                                },
                                "entryMode": "IccContactless"
                            }
                        }
                    ]
                }
            },
            "lifePhase": "Auth",
            "isPartial": false,
            "moneys": {
                "clear": {
                    "cashbackAmt": 2500,
                    "convRate": 1,
                    "ccy": 643,
                    "amt": 3024.98,
                    "convDate": "2026-08-10T00:00:00.000"
                },
                "cust": {
                    "cashbackAmt": 2500,
                    "ccy": 643,
                    "amt": 3024.98
                }
            },
            "preprocessOnly": false,
            "isBackward": false
        }
    },
    "lmdAttrs": {
        "system": "CCOP",
        "version": 1,
        "createTime": "2026-08-09T14:58:11.000+03:00",
        "updateTime": "2026-08-09T14:58:18.895+03:00"
    },
    "status": {
        "operationStatus": "New"
    }
}$jtestGOODSWITHCASHBACKoriginalauthauth$, $jtestGOODSWITHCASHBACKoriginalauthclr${
    "systemCode": "2218",
    "msName": "m095",
    "cbiRequest": {
        "tranRequest": {
            "parties": {
                "term": {
                    "caps": {
                        "pin": true,
                        "maxPinLen": 6,
                        "signAnalysis": false,
                        "cardCapture": true,
                        "attendance": true,
                        "locationKind": "BranchIndoor",
                        "mpos": false,
                        "mobile": false
                    },
                    "owner": {
                        "mcc": 5411,
                        "title": "PYATEROCHKA 2595",
                        "city": "N.NOVGOROD",
                        "country": 643,
                        "zip": "603095",
                        "rid": "990000016859",
                        "documents": {
                            "document": [
                                {
                                    "kind": "TaxNumber",
                                    "value": "7825706086"
                                }
                            ]
                        }
                    },
                    "rid": "10366242",
                    "type": "Pos",
                    "acquirerRid": "70372200101",
                    "forwarderRid": "70372200101"
                },
                "cust": {
                    "token": [
                        {
                            "card": {
                                "entryMode": "IccContactless",
                                "cardId": "87159b64-5447-4785-a419-719e39ce9426"
                            },
                            "kind": "Card"
                        }
                    ],
                    "auth": {
                        "signChecked": false,
                        "tdsChecked": false
                    },
                    "presence": true,
                    "organization": {
                        "documents": {
                            "document": [
                                {}
                            ]
                        },
                        "ccyResidenceCountries": []
                    }
                },
                "payee": {
                    "card": {},
                    "owner": {
                        "documents": {
                            "document": [
                                {}
                            ]
                        },
                        "ccyResidenceCountries": []
                    }
                }
            },
            "link": [
                {
                    "key": "F2E64E5137A17D3DBB8BD16D9A75A7C7250D3EA6260809430984961823",
                    "nrn": "6221674253806495"
                }
            ],
            "match": {
                "key": "b5d9600bd34f4664a77ad2c8b6785112"
            },
            "moneys": {
                "clear": {
                    "amt": 3024.98,
                    "ccy": 643
                },
                "cust": {
                    "amt": 3024.98,
                    "ccy": 643
                }
            },
            "specific": {
                "multiclearing": false,
                "isFinalPayment": false,
                "payment": {}
            },
            "userAttrs": {
                "paramValue": [
                    {
                        "val": "622155781364",
                        "rid": "extRrn"
                    },
                    {
                        "val": "59220016222479283984437",
                        "rid": "ARN"
                    },
                    {
                        "val": "MIR",
                        "rid": "MK_Inc_Channel"
                    },
                    {
                        "val": "09",
                        "rid": "MK_Inc_ProcCode"
                    }
                ]
            },
            "localTime": "2026-08-09T14:58:11.000",
            "undoState": "Normal",
            "kind": "GoodsWithCashback",
            "lifePhase": "Presentment",
            "isReversal": false,
            "isPartial": false,
            "isAdvice": true,
            "approvalCode": "UTXKC4"
        }
    },
    "lmdAttrs": {
        "system": "CCOP",
        "version": 1,
        "createTime": "2026-08-12T01:58:42.420488+03:00",
        "updateTime": "2026-08-12T01:58:42.423355082+03:00"
    },
    "status": {
        "operationStatus": "Received"
    },
    "rId": "c0464501-b38c-4a3a-b587-af02e44dd876"
}$jtestGOODSWITHCASHBACKoriginalauthclr$),
  ('test_GOODS_original_auth', 'GOODS original auth', $jtestGOODSoriginalauthauth${
    "rId": "69d72b72-706e-4b6d-aac3-54669fc3dd16",
    "userAgent": "RadixWare",
    "systemCode": "2218",
    "msName": "m263",
    "ip": "10.234.26.129",
    "cbiRequest": {
        "holdActions": {
            "action": [
                {
                    "accountClientId": 3145745,
                    "accountPlanCode": "004",
                    "origAmtDelta": 2122.5,
                    "amtDelta": 2122.5,
                    "accountContractRid": "fc_019f59c8-410d-7723-ad73-a7eb4f8767e0",
                    "origCcy": 643,
                    "accountNumber": "fc_019f59c8-410d-7723-ad73-a7eb4f8767e0",
                    "holdKind": "Auth",
                    "accountId": 2773690,
                    "accountCcy": 643,
                    "accountClientRid": "ul_1829922982",
                    "accountContractId": 7273918,
                    "actionKind": 1,
                    "accountPlanSubCode": "001",
                    "accountPlanGuid": "DGY5YENQEFH7RAKMQEKKRLOFDQ",
                    "holdId": 29767142,
                    "holdSign": -1
                }
            ]
        },
        "tranRequest": {
            "initiatorRid": "RTPAUTH",
            "userAttrs": {
                "mode": "SYNC",
                "seq": 0,
                "paramValue": [
                    {
                        "val": "VISA",
                        "rid": "MK_Inc_Channel"
                    },
                    {
                        "val": "00",
                        "rid": "MK_Inc_ProcCode"
                    },
                    {
                        "val": "100",
                        "rid": "MK_Inc_Type"
                    },
                    {
                        "val": "0",
                        "rid": "MK_Iss_Transitive"
                    },
                    {
                        "val": "622310840510",
                        "rid": "extRrn"
                    },
                    {
                        "val": "YES",
                        "title": "CERTINFO",
                        "rid": "CERT"
                    }
                ]
            },
            "undoState": "Normal",
            "kind": "Goods",
            "link": [],
            "match": {
                "acquirerRid": "465882",
                "storeToDoer": [],
                "checkForDuplicate": false,
                "linkageKind": "Normal",
                "nrn": "746223040588705",
                "key": "E34406DFFC7E181B6A012D954FA148FDF4A5BDD3260811371294331649",
                "rrn": "260811371294331649"
            },
            "rollbackOnAnyResult": false,
            "isAdvice": false,
            "refineRs": [],
            "isReversal": false,
            "specific": {
                "setupRestriction": [],
                "deferred": false,
                "fastFunds": false,
                "contractLink": [],
                "receiptRequested": false,
                "atcUpdate": false,
                "prepurchase": false
            },
            "networkSpecific": {
                "visa": {
                    "tranId": "746223040588705",
                    "netId": 2,
                    "posInfo": "0445000010",
                    "tranType": 0
                },
                "partnerRelease": "202210141000",
                "inactiveReleaseFeatures": []
            },
            "version": "3.2.42.10.19",
            "localTime": "2026-08-11T13:18:49.426",
            "originatorUnitId": 40,
            "originatorDay": "2026-08-12T00:00:00.000",
            "rollbackOnResults": [],
            "isMigration": false,
            "parties": {
                "term": {
                    "owner": {
                        "country": 643,
                        "city": "NABEREZHNYE C",
                        "ccyResidenceCountries": [],
                        "rid": "30021680",
                        "mcc": 5541,
                        "title": "IRBIS"
                    },
                    "total": [],
                    "acquirerRid": "465882",
                    "rid": "50029282",
                    "type": "Pos",
                    "acquirerCountry": 643,
                    "channelEncryption": false,
                    "usePaymentTransitAcct": false,
                    "caps": {
                        "signAnalysis": false,
                        "dsrp": false,
                        "tds": false,
                        "contactless": true,
                        "icc": true,
                        "mobile": false,
                        "interactive": false,
                        "cardCapture": false,
                        "barCode": false,
                        "singleTap": false,
                        "magWrite": false,
                        "pin": true,
                        "mpos": false,
                        "magRead": true,
                        "maxPinLen": 4,
                        "keyEntry": false,
                        "attendance": true,
                        "ocr": false
                    }
                },
                "cust": {
                    "contractRid": "fc_019f59c8-410d-7723-ad73-a7eb4f8767e0",
                    "presence": true,
                    "token": [
                        {
                            "kind": "Card",
                            "card": {
                                "auth": {
                                    "credentialCapturedForResults": [],
                                    "signChecked": true,
                                    "presence": true
                                },
                                "serviceCode": "201",
                                "plasticId": "15c812a5-edc4-4dd2-a842-6fd53be44369",
                                "cardId": "b8c89a63-3a79-4e7e-a606-8273cc8e5e4a",
                                "expDate": "2026-04-01T00:00:00.000",
                                "emv": {
                                    "mbr": 1
                                },
                                "entryMode": "IccContactless"
                            }
                        }
                    ]
                }
            },
            "lifePhase": "Auth",
            "isPartial": false,
            "moneys": {
                "clear": {
                    "convRate": 1,
                    "ccy": 643,
                    "amt": 2122.5
                },
                "cust": {
                    "ccy": 643,
                    "amt": 2122.5
                }
            },
            "preprocessOnly": false,
            "isBackward": false
        }
    },
    "lmdAttrs": {
        "system": "CCOP",
        "version": 1,
        "createTime": "2026-08-11T13:18:49.426+03:00",
        "updateTime": "2026-08-11T13:18:49.768+03:00"
    },
    "status": {
        "operationStatus": "New"
    }
}$jtestGOODSoriginalauthauth$, $jtestGOODSoriginalauthclr${
    "systemCode": "2218",
    "msName": "m095",
    "cbiRequest": {
        "tranRequest": {
            "parties": {
                "term": {
                    "caps": {
                        "icc": true,
                        "keyEntry": false,
                        "contactless": true,
                        "magRead": false,
                        "ocr": false,
                        "barCode": false
                    },
                    "owner": {
                        "mcc": 5541,
                        "title": "IRBIS",
                        "city": "NABEREZHNYE C",
                        "country": 643,
                        "zip": "00000",
                        "rid": "30021680",
                        "documents": {
                            "document": [
                                {}
                            ]
                        }
                    },
                    "rid": "50029282",
                    "type": "Pos",
                    "acquirerRid": "465882"
                },
                "cust": {
                    "token": [
                        {
                            "card": {
                                "entryMode": "IccContactless",
                                "cardId": "b8c89a63-3a79-4e7e-a606-8273cc8e5e4a"
                            },
                            "kind": "Card"
                        }
                    ],
                    "auth": {
                        "signChecked": false,
                        "tdsChecked": false
                    },
                    "presence": false,
                    "organization": {
                        "documents": {
                            "document": [
                                {}
                            ]
                        },
                        "ccyResidenceCountries": []
                    }
                },
                "payee": {
                    "card": {},
                    "owner": {
                        "documents": {
                            "document": [
                                {}
                            ]
                        },
                        "ccyResidenceCountries": []
                    }
                }
            },
            "link": [
                {
                    "key": "E34406DFFC7E181B6A012D954FA148FDF4A5BDD3260811371294331649",
                    "nrn": "746223040588705"
                }
            ],
            "match": {
                "key": "838e0f4320134748af3afd57eac8f5b3"
            },
            "moneys": {
                "clear": {
                    "amt": 2122.5,
                    "ccy": 643
                },
                "cust": {
                    "amt": 2122.5,
                    "ccy": 643
                }
            },
            "specific": {
                "multiclearing": false,
                "isFinalPayment": false,
                "payment": {}
            },
            "userAttrs": {
                "paramValue": [
                    {
                        "val": "74658826224135649166305",
                        "rid": "ARN"
                    },
                    {
                        "val": "VISA",
                        "rid": "MK_Inc_Channel"
                    },
                    {
                        "val": "050",
                        "rid": "MK_Inc_ProcCode"
                    }
                ]
            },
            "localTime": "2026-08-11T00:00:00.000",
            "undoState": "Normal",
            "kind": "Goods",
            "lifePhase": "Presentment",
            "isReversal": false,
            "isPartial": false,
            "isAdvice": true,
            "approvalCode": "ZUFFBF"
        }
    },
    "lmdAttrs": {
        "system": "CCOP",
        "version": 1,
        "createTime": "2026-08-12T11:32:25.513985+03:00",
        "updateTime": "2026-08-12T11:32:25.516697616+03:00"
    },
    "status": {
        "operationStatus": "Received"
    },
    "rId": "a797ae70-6557-4fd7-8e02-5776f46231dc"
}$jtestGOODSoriginalauthclr$),
  ('test_PAYMENT_CREDIT_original_auth', 'PAYMENT CREDIT original auth', $jtestPAYMENTCREDIToriginalauthauth${
    "rId": "41c035b5-9e38-4fd8-aca4-cc89278ca980",
    "userAgent": "RadixWare",
    "systemCode": "2218",
    "msName": "m263",
    "ip": "10.234.26.129",
    "cbiRequest": {
        "holdActions": {
            "action": [
                {
                    "accountClientId": 2563850,
                    "accountPlanCode": "004",
                    "origAmtDelta": -6007.4,
                    "amtDelta": -6007.4,
                    "accountContractRid": "fc_019dceae-da46-7852-8f76-afc94ed4e2bb",
                    "origCcy": 643,
                    "accountNumber": "fc_019dceae-da46-7852-8f76-afc94ed4e2bb",
                    "holdKind": "Auth",
                    "accountId": 2449105,
                    "accountCcy": 643,
                    "accountClientRid": "ul_1000236155778",
                    "accountContractId": 6595425,
                    "actionKind": 1,
                    "accountPlanSubCode": "001",
                    "accountPlanGuid": "DGY5YENQEFH7RAKMQEKKRLOFDQ",
                    "holdId": 28415829,
                    "holdSign": -1
                }
            ]
        },
        "tranRequest": {
            "initiatorRid": "RTPAUTH",
            "userAttrs": {
                "mode": "SYNC",
                "seq": 0,
                "paramValue": [
                    {
                        "val": "MIR",
                        "rid": "MK_Inc_Channel"
                    },
                    {
                        "val": "28",
                        "rid": "MK_Inc_ProcCode"
                    },
                    {
                        "val": "100",
                        "rid": "MK_Inc_Type"
                    },
                    {
                        "val": "812",
                        "rid": "MK_Inc_F22"
                    },
                    {
                        "val": "B2B",
                        "rid": "MK_Inc_F4814"
                    },
                    {
                        "val": "643",
                        "rid": "MK_P2P_OppositeCountry"
                    },
                    {
                        "val": "0",
                        "rid": "MK_Iss_Transitive"
                    },
                    {
                        "val": "620503744818",
                        "rid": "extRrn"
                    },
                    {
                        "val": "YES",
                        "title": "CERTINFO",
                        "rid": "CERT"
                    }
                ]
            },
            "undoState": "Normal",
            "kind": "PaymentCredit",
            "link": [],
            "match": {
                "acquirerRid": "70800700102",
                "storeToDoer": [],
                "checkForDuplicate": false,
                "linkageKind": "Normal",
                "nrn": "6205131277386828",
                "key": "5F6BF3EE33454832B424F5C29B47836FA3A0E6D5260724114167535175",
                "rrn": "260724114167535175"
            },
            "rollbackOnAnyResult": false,
            "isAdvice": false,
            "refineRs": [],
            "isReversal": false,
            "specific": {
                "setupRestriction": [],
                "custInfo": {
                    "subjectDocListFilter": {
                        "docKindList": [
                            "TaxNumber"
                        ]
                    },
                    "kinds": [
                        "PayeeTokenOwnerCcyResidenceCountries",
                        "PayeeTokenOwnerDocSeries",
                        "PayeeTokenOwnerDocRid"
                    ]
                },
                "deferred": false,
                "fastFunds": true,
                "payment": {
                    "purposeText": "от агента принципалу Байкову Д И Эл. Договор 5 571 от 27.02.2025",
                    "returnAllParams": false,
                    "operType": "Business2Business"
                },
                "contractLink": [],
                "receiptRequested": false,
                "atcUpdate": false,
                "prepurchase": false
            },
            "networkSpecific": {
                "nspk": {
                    "trn": 6205131277386828,
                    "messageType": 100,
                    "posData": "100050053129301    ",
                    "tranFraudIndicator": "0000000000000000000000000000000000000000",
                    "actualEci": "3",
                    "cardProductId": "PPB",
                    "posEntryMode": "812",
                    "networkIdentifier": "0001"
                },
                "inactiveReleaseFeatures": []
            },
            "version": "3.2.42.10.19",
            "localTime": "2026-07-24T06:10:15.000",
            "originatorUnitId": 55,
            "originatorDay": "2026-07-25T00:00:00.000",
            "rollbackOnResults": [],
            "isMigration": false,
            "parties": {
                "payee": {
                    "owner": {
                        "ccyResidenceCountries": [],
                        "rid": "2563850"
                    },
                    "usePayerToken": false,
                    "contractFromTemplateTran": false,
                    "rids": [],
                    "contractRid": "fc_019dceae-da46-7852-8f76-afc94ed4e2bb",
                    "type": "CustToken",
                    "card": {
                        "pan": "**************48",
                        "cardFromTemplateTran": false
                    }
                },
                "term": {
                    "owner": {
                        "zip": "129301",
                        "country": 643,
                        "city": "Moskva",
                        "ccyResidenceCountries": [],
                        "rid": "200000001479940",
                        "mcc": 6536,
                        "title": "Element"
                    },
                    "total": [],
                    "acquirerRid": "70800700102",
                    "rid": "25572157",
                    "type": "Ecom",
                    "channelEncryption": false,
                    "usePaymentTransitAcct": false,
                    "forwarderRid": "70802007001",
                    "caps": {
                        "signAnalysis": false,
                        "dsrp": false,
                        "contactless": false,
                        "goodsAuthOnly": false,
                        "icc": false,
                        "mobile": true,
                        "interactive": false,
                        "cardCapture": false,
                        "barCode": false,
                        "singleTap": false,
                        "magWrite": false,
                        "mpos": false,
                        "magRead": false,
                        "maxPinLen": 4,
                        "keyEntry": true,
                        "ocr": false
                    }
                },
                "cust": {
                    "auth": {
                        "photoChecked": false,
                        "regTrustedMerchant": false,
                        "biometricsChecked": false,
                        "signChecked": false,
                        "personIdChecked": false
                    },
                    "organization": {
                        "documents": {
                            "document": [
                                {
                                    "kind": "TaxNumber",
                                    "value": "5029290051"
                                }
                            ]
                        },
                        "ccyResidenceCountries": [
                            643
                        ]
                    },
                    "token": [
                        {
                            "kind": "Card",
                            "card": {
                                "auth": {
                                    "credentialCapturedForResults": [],
                                    "presence": false
                                },
                                "plasticId": "6fb2bd06-15ff-4109-a78b-e4a9719c9c52",
                                "cardId": "5d57c3bf-5808-4ece-8e6f-d74aafeaeb0f"
                            }
                        }
                    ]
                }
            },
            "lifePhase": "Auth",
            "isPartial": false,
            "moneys": {
                "clear": {
                    "convRate": 1,
                    "ccy": 643,
                    "amt": 6007.4,
                    "convDate": "2026-07-24T00:00:00.000"
                },
                "cust": {
                    "ccy": 643,
                    "amt": 6007.4
                }
            },
            "preprocessOnly": false,
            "isBackward": false
        }
    },
    "lmdAttrs": {
        "system": "CCOP",
        "version": 1,
        "createTime": "2026-07-24T06:10:15.000+03:00",
        "updateTime": "2026-07-24T06:10:17.075+03:00"
    },
    "status": {
        "operationStatus": "New"
    }
}$jtestPAYMENTCREDIToriginalauthauth$, $jtestPAYMENTCREDIToriginalauthclr${
    "systemCode": "2218",
    "msName": "m095",
    "cbiRequest": {
        "tranRequest": {
            "parties": {
                "term": {
                    "caps": {
                        "icc": false,
                        "keyEntry": true,
                        "contactless": false,
                        "magRead": false,
                        "ocr": false,
                        "barCode": false,
                        "pin": false,
                        "maxPinLen": 0,
                        "signAnalysis": false,
                        "cardCapture": false,
                        "mpos": false,
                        "mobile": true,
                        "interactive": false
                    },
                    "owner": {
                        "mcc": 6536,
                        "title": "Element",
                        "city": "Moskva",
                        "country": 643,
                        "zip": "129301",
                        "rid": "200000001479940",
                        "documents": {
                            "document": [
                                {}
                            ]
                        }
                    },
                    "rid": "25572157",
                    "type": "Man",
                    "acquirerRid": "70800700102",
                    "forwarderRid": "70802007001"
                },
                "cust": {
                    "token": [
                        {
                            "card": {
                                "entryMode": "ECommerce",
                                "cardId": "5d57c3bf-5808-4ece-8e6f-d74aafeaeb0f"
                            },
                            "kind": "Card"
                        }
                    ],
                    "auth": {
                        "tdsChecked": false
                    },
                    "presence": false,
                    "organization": {
                        "documents": {
                            "document": [
                                {
                                    "kind": "TaxNumber",
                                    "value": "504791770730"
                                }
                            ]
                        },
                        "ccyResidenceCountries": [
                            643
                        ]
                    }
                },
                "payee": {
                    "card": {},
                    "owner": {
                        "documents": {
                            "document": [
                                {
                                    "kind": "TaxNumber",
                                    "value": "5029290051"
                                }
                            ]
                        },
                        "ccyResidenceCountries": [
                            643
                        ]
                    }
                }
            },
            "link": [
                {
                    "key": "5F6BF3EE33454832B424F5C29B47836FA3A0E6D5260724114167535175",
                    "nrn": "6205131277386828"
                }
            ],
            "match": {
                "key": "fa289aa67fc543a4aa1cb9c9e5acfcf4"
            },
            "moneys": {
                "clear": {
                    "amt": 6007.4,
                    "ccy": 643
                },
                "cust": {
                    "amt": 6007.4,
                    "ccy": 643
                }
            },
            "specific": {
                "multiclearing": false,
                "isFinalPayment": false,
                "payment": {
                    "purposeText": "Ð¾Ñ Ð°Ð³ÐµÐ½ÑÐ° Ð¿ÑÐ¸Ð½ÑÐ¸Ð¿Ð°Ð»Ñ ÐÐ°Ð¹ÐºÐ¾Ð²Ñ Ð Ð Ð­Ð». ÐÐ¾Ð³Ð¾Ð²Ð¾Ñ 5Â 571 Ð¾Ñ 27.02.2025",
                    "operType": "Business2Business"
                }
            },
            "userAttrs": {
                "paramValue": [
                    {
                        "val": "620503744818",
                        "rid": "extRrn"
                    },
                    {
                        "val": "09070006205310037448189",
                        "rid": "ARN"
                    },
                    {
                        "val": "MIR",
                        "rid": "MK_Inc_Channel"
                    },
                    {
                        "val": "28",
                        "rid": "MK_Inc_ProcCode"
                    }
                ]
            },
            "localTime": "2026-07-24T06:10:15.000",
            "undoState": "Normal",
            "kind": "PaymentCredit",
            "lifePhase": "Presentment",
            "isReversal": false,
            "isPartial": false,
            "isAdvice": true,
            "approvalCode": "8M9H3P"
        }
    },
    "lmdAttrs": {
        "system": "CCOP",
        "version": 1,
        "createTime": "2026-07-24T15:06:19.249937+03:00",
        "updateTime": "2026-07-24T15:06:19.252767925+03:00"
    },
    "status": {
        "operationStatus": "Received"
    },
    "rId": "3192f39a-ab87-4cc4-ada5-1cf7d928c0b7"
}$jtestPAYMENTCREDIToriginalauthclr$),
  ('test_PAYMENT_DEBIT_original_auth', 'PAYMENT DEBIT original auth', $jtestPAYMENTDEBIToriginalauthauth${
    "rId": "4df605fc-d905-4414-b95b-c5087bfd0cd2",
    "userAgent": "RadixWare",
    "systemCode": "2218",
    "msName": "m263",
    "ip": "10.234.26.129",
    "cbiRequest": {
        "holdActions": {
            "action": [
                {
                    "accountClientId": 2850879,
                    "accountPlanCode": "004",
                    "origAmtDelta": 83,
                    "amtDelta": 83,
                    "accountContractRid": "fc_019eddb7-4f34-7b6b-8fbe-e955243d13c7",
                    "origCcy": 643,
                    "accountNumber": "fc_019eddb7-4f34-7b6b-8fbe-e955243d13c7",
                    "holdKind": "Auth",
                    "accountId": 2615197,
                    "accountCcy": 643,
                    "accountClientRid": "ul_1000268158114",
                    "accountContractId": 6949909,
                    "actionKind": 1,
                    "accountPlanSubCode": "001",
                    "accountPlanGuid": "DGY5YENQEFH7RAKMQEKKRLOFDQ",
                    "holdId": 29615659,
                    "holdSign": -1
                }
            ]
        },
        "tranRequest": {
            "initiatorRid": "RTPAUTH",
            "userAttrs": {
                "mode": "SYNC",
                "seq": 0,
                "paramValue": [
                    {
                        "val": "W4",
                        "rid": "MK_Inc_Channel"
                    },
                    {
                        "val": "00",
                        "rid": "MK_Inc_ProcCode"
                    },
                    {
                        "val": "100",
                        "rid": "MK_Inc_Type"
                    },
                    {
                        "val": "FC",
                        "rid": "W4_48_889"
                    },
                    {
                        "val": "0",
                        "rid": "W4_25"
                    },
                    {
                        "val": "643",
                        "rid": "MK_P2P_OppositeCountry"
                    },
                    {
                        "val": "0",
                        "rid": "MK_Iss_Transitive"
                    },
                    {
                        "val": "622115129204",
                        "rid": "extRrn"
                    },
                    {
                        "val": "YES",
                        "title": "CERTINFO",
                        "rid": "CERT"
                    }
                ]
            },
            "undoState": "Normal",
            "kind": "PaymentDebit",
            "link": [],
            "match": {
                "acquirerRid": "888877",
                "storeToDoer": [],
                "checkForDuplicate": false,
                "linkageKind": "Normal",
                "nrn": "5177711829239921",
                "key": "EAD67AB4D3E5FA7D1FB8F33A12757FB888D19716260809372155752788",
                "rrn": "260809372155752788"
            },
            "rollbackOnAnyResult": false,
            "isAdvice": false,
            "refineRs": [],
            "isReversal": false,
            "specific": {
                "setupRestriction": [],
                "deferred": false,
                "fastFunds": false,
                "contractLink": [],
                "receiptRequested": false,
                "atcUpdate": false,
                "prepurchase": false
            },
            "networkSpecific": {
                "amex": {
                    "tid": "5177711829239921"
                },
                "way4": {
                    "w447912": "2",
                    "w447925": "5177711829239921",
                    "w447958": "000200220",
                    "w447938": "5177711829239921"
                },
                "nspk": {
                    "trn": 5177711829239921
                },
                "visa": {
                    "tranId": "5177711829239921"
                },
                "dinersClub": {
                    "networkReferenceId": "5177711829239921"
                },
                "inactiveReleaseFeatures": [],
                "masterCard": {
                    "traceId": "5177711829239921",
                    "cmitindicator": "C101",
                    "onBehalf": []
                }
            },
            "version": "3.2.42.10.19",
            "localTime": "2026-08-08T20:50:24.000",
            "originatorUnitId": 41,
            "originatorDay": "2026-08-10T00:00:00.000",
            "rollbackOnResults": [],
            "isMigration": false,
            "parties": {
                "payee": {
                    "owner": {
                        "ccyResidenceCountries": [],
                        "mcc": 4111
                    },
                    "usePayerToken": false,
                    "contractFromTemplateTran": false,
                    "rids": [],
                    "type": "Vendor"
                },
                "term": {
                    "owner": {
                        "zip": "129110",
                        "country": 643,
                        "city": "MOSKVA",
                        "ccyResidenceCountries": [],
                        "rid": "249999373",
                        "mcc": 4111,
                        "title": "Mos.Transport"
                    },
                    "total": [],
                    "acquirerRid": "888877",
                    "rid": "W0011087",
                    "type": "Adm",
                    "acquirerCountry": 643,
                    "channelEncryption": false,
                    "usePaymentTransitAcct": false,
                    "caps": {
                        "signAnalysis": false,
                        "dsrp": false,
                        "tds": false,
                        "contactless": true,
                        "icc": false,
                        "mobile": false,
                        "cardCapture": false,
                        "barCode": false,
                        "singleTap": false,
                        "magWrite": false,
                        "pin": false,
                        "mpos": false,
                        "magRead": true,
                        "maxPinLen": 4,
                        "partialApproval": false,
                        "keyEntry": false,
                        "attendance": false,
                        "ocr": false
                    }
                },
                "cust": {
                    "payerType": "Debit",
                    "auth": {
                        "photoChecked": false,
                        "regTrustedMerchant": false,
                        "biometricsChecked": false,
                        "signChecked": false,
                        "personIdChecked": false
                    },
                    "contractRid": "fc_019eddb7-4f34-7b6b-8fbe-e955243d13c7",
                    "person": {
                        "documents": {
                            "document": []
                        },
                        "ccyResidenceCountries": []
                    },
                    "organization": {
                        "ccyResidenceCountries": [],
                        "rid": "2850879"
                    },
                    "presence": true,
                    "token": [
                        {
                            "kind": "Card",
                            "card": {
                                "auth": {
                                    "credentialCaptured": true,
                                    "credentialCapturedForResults": [],
                                    "tdsChecked": false,
                                    "signChecked": false,
                                    "presence": true
                                },
                                "serviceCode": "201",
                                "plasticId": "e2ef0b87-49fa-4354-b0b8-d918cd26629e",
                                "cardId": "3ee8bc05-050e-4309-afc6-698c7dc5c6c2",
                                "expDate": "2028-06-01T00:00:00.000",
                                "emv": {
                                    "mbr": 90
                                },
                                "entryMode": "IccContactless"
                            }
                        }
                    ]
                }
            },
            "lifePhase": "Auth",
            "isPartial": false,
            "moneys": {
                "clear": {
                    "convRate": 1,
                    "ccy": 643,
                    "amt": 83
                },
                "cust": {
                    "surcharges": {
                        "surcharge": [],
                        "confirmationMode": "None",
                        "externalAmt": 0,
                        "toTakeCash": false
                    },
                    "ccy": 643,
                    "amt": 83
                }
            },
            "preprocessOnly": false,
            "isBackward": false
        }
    },
    "lmdAttrs": {
        "system": "CCOP",
        "version": 1,
        "createTime": "2026-08-08T20:50:24.000+03:00",
        "updateTime": "2026-08-09T13:20:15.886+03:00"
    },
    "status": {
        "operationStatus": "New"
    }
}$jtestPAYMENTDEBIToriginalauthauth$, $jtestPAYMENTDEBIToriginalauthclr${
    "systemCode": "2218",
    "msName": "m095",
    "cbiRequest": {
        "tranRequest": {
            "kind": "Goods",
            "lifePhase": "Presentment",
            "localTime": "2026-08-08T00:00:00.000",
            "undoState": "Normal",
            "isReversal": false,
            "isPartial": false,
            "isAdvice": true,
            "parties": {
                "term": {
                    "rid": "W0011087",
                    "type": "Pos",
                    "owner": {
                        "country": 643,
                        "city": "MOSKVA",
                        "mcc": 4111,
                        "title": "MOS.TRANSPORT",
                        "zip": "00000",
                        "rid": "249999373"
                    },
                    "caps": {
                        "icc": true,
                        "keyEntry": false,
                        "contactless": true,
                        "magRead": false,
                        "ocr": false,
                        "barCode": false
                    },
                    "acquirerRid": "888877"
                },
                "cust": {
                    "presence": false,
                    "auth": {
                        "signChecked": true
                    },
                    "token": [
                        {
                            "card": {
                                "entryMode": "IccContactless",
                                "cardId": "3ee8bc05-050e-4309-afc6-698c7dc5c6c2"
                            },
                            "kind": "Card"
                        }
                    ]
                }
            },
            "match": {
                "key": "019fe68e-3e87-76f3-ae5b-9cd8200ee252"
            },
            "link": [
                {
                    "key": "EAD67AB4D3E5FA7D1FB8F33A12757FB888D19716260809372155752788"
                }
            ],
            "moneys": {
                "clear": {
                    "amt": 83.0,
                    "ccy": 643
                },
                "cust": {
                    "amt": 83.0,
                    "ccy": 643
                }
            },
            "specific": {
                "multiclearing": false,
                "isFinalPayment": false
            },
            "userAttrs": {
                "paramValue": [
                    {
                        "val": "78888876221351107695336",
                        "rid": "ARN"
                    },
                    {
                        "val": "W4",
                        "rid": "MK_Inc_Channel"
                    },
                    {
                        "val": "050",
                        "rid": "MK_Inc_ProcCode"
                    }
                ]
            },
            "approvalCode": "3VR5HS"
        }
    },
    "lmdAttrs": {
        "system": "CCOP",
        "version": 1,
        "createTime": "2026-08-09T15:45:45.490032+03:00",
        "updateTime": "2026-08-09T15:45:45.493804912+03:00"
    },
    "status": {
        "operationStatus": "Received"
    },
    "rId": "019fe68e-3e87-76f3-ae5b-9cd8200ee252"
}$jtestPAYMENTDEBIToriginalauthclr$);
