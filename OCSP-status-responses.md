OCSP requests could have different responses if submitted to the CA's internal OCSP or to the OCSP subsystem. The possible cases are:


|   CA Certificate  |  Serial    |   Internal OCSP |  OCSP Subsystem  |
|-------------------|------------|-----------------|------------------|
|    Valid          |  Valid     |     Good        |     Good         |
|    Not Valid      |  Valid     |     Unknown     |     Unknown      |
|    Valid          |  Revoked   |     Revoked     |     Revoked      |
|    Valid          |  Not valid |   **Unknown**   |   **Good**       |


For the case were subject is not valid (e.g. do not exist) the **Good** answer is accepted by the specification [rfc6960](https://www.rfc-editor.org/rfc/rfc6960) because it is not revoked.