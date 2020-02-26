/*
 * This file renders the OutputFilePreview component for analyses
 */

import React, { Suspense, useContext, useEffect } from "react";
import { Button, Dropdown, Icon, Menu, Tabs } from "antd";
import { AnalysisContext } from "../../../../contexts/AnalysisContext";
import { AnalysisOutputsContext } from "../../../../contexts/AnalysisOutputsContext";

import { TabPaneContent } from "../../../../components/tabs/TabPaneContent";
import { ContentLoading } from "../../../../components/loader/ContentLoading";
import { AnalysisTabularPreview } from "../AnalysisTabularPreview";
import { WarningAlert } from "../../../../components/alerts/WarningAlert";
import { SPACE_XS } from "../../../../styles/spacing";
import { downloadFilesAsZip, downloadOutputFile } from "../../../../apis/analysis/analysis";
import { blue5, grey7 } from "../../../../styles/colors";
import styled from "styled-components";

const ZipDownloadButton = styled(Button)`
  color: ${grey7};

  &:hover {
    color: ${blue5};
  }
`;

const AnalysisTextPreview = React.lazy(() => import("../AnalysisTextPreview"));
const AnalysisJsonPreview = React.lazy(() => import("../AnalysisJsonPreview"));

const { TabPane } = Tabs;

export default function OutputFilePreview() {
  const {
    analysisOutputsContext,
    getAnalysisOutputs,
    jsonExtSet,
    tabExtSet,
    blacklistExtSet
  } = useContext(AnalysisOutputsContext);

  const { analysisContext } = useContext(AnalysisContext);

  useEffect(() => {
    if (analysisOutputsContext.outputs === null) {
      getAnalysisOutputs();
    }
  }, []);

  function getMenu () {
    let fileNames = [];
    for (const output of analysisOutputsContext.outputs) {
      fileNames.push(<Menu.Item key={output.id}>{output.filename}</Menu.Item>);
    }
    return <Menu onClick={(e) => downloadOutputFile({submissionId: analysisContext.analysis.identifier, fileId: e.key})}>{fileNames}</Menu>;
  }

  function jsonOutputPreview() {
    let jsonOutput = [];

    for (const output of analysisOutputsContext.outputs) {
      if (!output.hasOwnProperty("fileExt") || !output.hasOwnProperty("id")) {
        continue;
      }

      if (jsonExtSet.has(output.fileExt)) {
        jsonOutput.unshift(
          <AnalysisJsonPreview output={output} key={output.filename} />
        );
      }
    }
    return jsonOutput;
  }

  function tabularOutputPreview() {
    let tabularOutput = [];

    for (const output of analysisOutputsContext.outputs) {
      if (!output.hasOwnProperty("fileExt") || !output.hasOwnProperty("id")) {
        continue;
      }
      if (tabExtSet.has(output.fileExt)) {
        tabularOutput.unshift(
          <AnalysisTabularPreview output={output} key={output.filename} />
        );
      }
    }
    return tabularOutput;
  }

  function textOutputPreview() {
    let textOutput = [];

    for (const output of analysisOutputsContext.outputs) {
      if (!output.hasOwnProperty("fileExt") || !output.hasOwnProperty("id")) {
        continue;
      }
      if (!jsonExtSet.has(output.fileExt) && !tabExtSet.has(output.fileExt) && !blacklistExtSet.has(output.fileExt)) {
        textOutput.unshift(
          <AnalysisTextPreview output={output} key={output.filename} />
        );
      }
    }
    return textOutput;
  }

  return analysisOutputsContext.outputs !== null ? (
    <TabPaneContent title={i18n("AnalysisOutputs.outputFilePreview")}>
      {analysisOutputsContext.outputs.length > 0 ? (
        <div>
          <Dropdown.Button id="t-download-all-files-btn" style={{ marginBottom: SPACE_XS }} onClick={() => downloadFilesAsZip(analysisContext.analysis.identifier)} overlay={getMenu()}>
              <i
                className="fas fa-file-archive"
                style={{ marginRight: SPACE_XS }}
              ></i>
              {i18n("AnalysisOutputs.downloadAllFiles")}
          </Dropdown.Button>
          <Tabs defaultActiveKey="1" animated={false}>
            {analysisOutputsContext.fileTypes[0].hasTabularFile ? (
              <TabPane
                tab={i18n("AnalysisOutputs.tabularOutput")}
                key="tab-output"
              >
                {tabularOutputPreview()}
              </TabPane>
            ) : null}

            {analysisOutputsContext.fileTypes[0].hasTextFile ? (
              <TabPane
                tab={i18n("AnalysisOutputs.textOutput")}
                key="text-output"
              >
                <Suspense fallback={<ContentLoading />}>
                  {textOutputPreview()}
                </Suspense>
              </TabPane>
            ) : null}

            {analysisOutputsContext.fileTypes[0].hasJsonFile ? (
              <TabPane
                tab={i18n("AnalysisOutputs.jsonOutput")}
                key="json-output"
              >
                <Suspense fallback={<ContentLoading />}>
                  {jsonOutputPreview()}
                </Suspense>
              </TabPane>
            ) : null}
          </Tabs>
        </div>
      ) : (
        <WarningAlert message={i18n("AnalysisOutputs.noOutputsAvailable")} />
      )}
    </TabPaneContent>
  ) : (
    <ContentLoading />
  );
}
