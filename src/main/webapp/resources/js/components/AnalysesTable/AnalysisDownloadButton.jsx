import React, { useContext, useState } from "react";
import { Button } from "antd";

import { useInterval } from "../../hooks";
import { setBaseUrl } from "../../utilities/url-utilities";
import { IconDownloadFile } from "../icons/Icons";
import { AnalysesTableContext } from "../../contexts/AnalysesTableContext";

/**
 * Display the download results button for an analysis
 * @param {string} state The state of the analysis
 * @param {number} analysisId the analysis identifier
 * @param {number} updateDelay the polling delay
 * @returns {*}
 * @constructor
 */
export function AnalysisDownloadButton({ state, analysisId, updateDelay }) {
  const [disableDownloadButton, setDisableDownloadButton] = useState(
    state !== "COMPLETED"
  );
  const { analysesTableContext } = useContext(AnalysesTableContext);

  // Update if an analysis is completed (to enable or disable download results button)
  const intervalId = useInterval(() => {
    if (state !== "COMPLETED" && state !== "ERROR") {
      let rowData = analysesTableContext.rows.filter(
        (row) => row.identifier === analysisId
      );
      let currRowData = rowData[rowData.length - 1];
      if (typeof currRowData !== "undefined") {
        if (currRowData.isCompleted) {
          setDisableDownloadButton(false);
        }

        if (currRowData.isCompleted || currRowData.isError) {
          clearInterval(intervalId);
        }
      }
    } else {
      clearInterval(intervalId);
    }
  }, updateDelay);

  return (
    <Button
      shape="circle"
      disabled={disableDownloadButton}
      href={setBaseUrl(`ajax/analyses/download/${analysisId}`)}
      download
    >
      <IconDownloadFile />
    </Button>
  );
}
