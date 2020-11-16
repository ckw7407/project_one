import {App} from "./app.js";
import {LoginButton} from "./components/LoginButton.js";
import { ItemContentLoader } from "./components/ItemContentLoader.js";
import { CardStyleSheetBuilder } from "./components/CardStyleSheetBuilder.js";

/**
 * ==============================================
 * 아이템 페이지 구현
 * ==============================================
 */
class ItemPage extends App {
    initMembers() {
        super.initMembers();

        /**
         * 동적으로 삽입할 페이지의 경로를 기입해주세요.
         */
        this._pendingList = [
            {
                src: `pages/login.html`,
                parent: ".container",
                isCreateNewDiv: true,
            },
            {
                src: `pages/shop.html`,
                parent: ".contents-wrapper",
                isCreateNewDiv: false,
            }
        ];

        this._menuIndex = 1;
        
    }

    createNewStyleSheet(dataID, imagePath) {
    
        CardStyleSheetBuilder.builder(this, `

            .card {
                display: flex;
                flex-wrap: wrap;
                flex: auto 1 auto;
                flex-direction: column;
                border: 1px solid #F2F5F9;
            }
            
            .card p[${dataID}]::before {
                content: "";
                width: 100%;
                height: 78%;
                background: url("${imagePath}") left top;
                background-size: cover;
                background-repeat: no-repeat;
                position: absolute;
                border-radius: 0;
                left: 0;
                top: 0;
                z-index: 0;
            }

            .card p[${dataID}]:hover::before {
                filter: brightness(1.1);
                border-radius: 0;
                transition: all .2s linear;
            }

        `, dataID).run();


        const adminWrite = $("<div></div>", ".container");

        adminWrite.css({
            "borderRadius": "50%",
            "backgroundColor": "gray"
        });
    }    

    addEventListeners() {
        this.on("loginView:ready", () => LoginButton.builder().run());      
        this.on("contents:ready", () => ItemContentLoader.builder(this).run());        
    }

    onLoad() {
        super.onLoad();

        this.emit("loginView:ready");
        this.emit("contents:ready"); 
    }

}

const app = new ItemPage();
app.on("ready", async () => app.createLazyLoader());

window.app = app;
window.addEventListener("load", () => app.emit("ready"));