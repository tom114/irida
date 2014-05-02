var IndexPage = require("./pages/index_page");

describe("Index", function () {
    var page = new IndexPage();

    it('Should say welcome', function () {
        page.visitPage();

        expect(page.title.getText()).toEqual('Welcome to the IRIDA Platform!');
    });
});