/* eslint new-cap: [2, {"capIsNewExceptions": ["DataTable"]}] */
/**
 * @file AngularJS Component for display rows from the metadata file that do not match
 * Sample names on the server.
 */
const $ = require('jquery');
require('datatables.net');
require('datatables-bootstrap3-plugin');
require('style!datatables-bootstrap3-plugin/media/css/datatables-bootstrap3.css');
import {dom, formatBasicHeaders} from '../../../../utilities/datatables.utilities';

const resultsMissingComponent = {
  templateUrl: 'results.missing.component.tmpl.html',
  bindings: {
    rows: '=',
    headers: '='
  },
  controller() {
    this.$onInit = () => {
      if (this.rows.length > 0) {
        const columns = formatBasicHeaders(this.headers);
        $('#missing-table').DataTable({
          dom,
          scrollX: true,
          sScrollX: '100%',
          data: this.rows,
          columns
        });
      }
    };
  }
};

export default resultsMissingComponent;