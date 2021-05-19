import {
  Button,
  Card,
  Col,
  Form,
  InputNumber,
  Modal,
  notification,
  Row,
  Statistic,
  Typography,
} from "antd";
import isNumeric from "antd/es/_util/isNumeric";
import React from "react";
import {
  useGetProjectDetailsQuery,
  useUpdateProjectCoverageMutation,
} from "../../../../../apis/projects/project";

/**
 * Display and allow managers to be able to modify the minimum and maximum
 * coverage as well as the genome size.
 * @param {number} projectId - project identifier
 * @param {boolean} canManage - if the usr can manage this project
 * @returns {JSX.Element}
 * @constructor
 */
export function ProcessingCoverage({ projectId, canManage = false }) {
  const {
    data: { coverage = {} },
    isLoading,
  } = useGetProjectDetailsQuery(projectId);
  const [updateProjectCoverage] = useUpdateProjectCoverageMutation();

  const NOT_SET = i18n("ProcessingCoverage.not-set");
  const [visible, setVisible] = React.useState(false);
  const [form] = Form.useForm();

  const numericValidator = () => ({
    validator(rule, value) {
      if (!value || isNumeric(value)) {
        return Promise.resolve();
      }
      return Promise.reject(i18n("ProcessingCoverage.numeric"));
    },
  });

  const update = () =>
    form.validateFields().then((coverage) => {
      updateProjectCoverage({ projectId, coverage })
        .then((response) => {
          notification.success({ message: response.data.message });
        })
        .catch((error) => {
          notification.error({ message: error.response.data.error });
        })
        .finally(() => setVisible(false));
    });

  return (
    <section>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
      >
        <Typography.Title level={3}>
          {i18n("ProcessingCoverage.title")}
        </Typography.Title>
        {canManage && (
          <span>
            <Button onClick={() => setVisible(true)}>
              {i18n("form.btn.edit")}
            </Button>
            <Modal
              title={i18n("ProcessingCoverage.modal.title")}
              visible={visible}
              onCancel={() => setVisible(false)}
              onOk={update}
            >
              <Form
                layout="vertical"
                initialValues={{
                  minimum: coverage.minimum > -1 ? coverage.minimum : null,
                  maximum: coverage.maximum > -1 ? coverage.maximum : null,
                  genomeSize:
                    coverage.genomeSize > -1 ? coverage.genomeSize : null,
                }}
                form={form}
              >
                <Form.Item
                  label={i18n("ProcessingCoverage.minimum")}
                  name="minimum"
                  precision={0}
                  rules={[numericValidator]}
                >
                  <InputNumber style={{ width: `100%` }} step={100} min={0} />
                </Form.Item>
                <Form.Item
                  label={i18n("ProcessingCoverage.maximum")}
                  name="maximum"
                  rules={[numericValidator]}
                >
                  <InputNumber style={{ width: `100%` }} step={100} min={0} />
                </Form.Item>
                <Form.Item
                  label={i18n("ProcessingCoverage.genomeSize")}
                  name="genomeSize"
                  rules={[numericValidator]}
                >
                  <InputNumber style={{ width: `100%` }} step={100} min={0} />
                </Form.Item>
              </Form>
            </Modal>
          </span>
        )}
      </div>
      <Row gutter={16}>
        <Col span={8}>
          <Card>
            <Statistic
              loading={isLoading}
              title={i18n("ProcessingCoverage.minimum")}
              value={coverage.minimum > -1 ? coverage.minimum : NOT_SET}
              suffix={coverage.minimum > -1 ? "X" : ""}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              loading={isLoading}
              title={i18n("ProcessingCoverage.maximum")}
              value={coverage.maximum > -1 ? coverage.maximum : NOT_SET}
              suffix={coverage.maximum > -1 ? "X" : ""}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              loading={isLoading}
              title={i18n("ProcessingCoverage.genomeSize")}
              value={coverage.genomeSize > -1 ? coverage.genomeSize : NOT_SET}
              suffix={coverage.genomeSize > -1 ? "BP" : ""}
            />
          </Card>
        </Col>
      </Row>
    </section>
  );
}