
let stompClient = null;

const ADMIN = 'ADMIN';
const USER = 'USER';


function connOrDisConn(conn) {
    // 웹소켓 연결 시 연결버튼 비활성화 속성 추가
    $("#connectBtn").prop("disabled", conn); // 유저 연결버튼
    $("#adminConnectBtn").prop("disabled", conn); // 관리자 연결버튼

    // 웹소켓 연결 종료 시 연결 종료 버튼 비활성화 속성 추가
    $("#disconnectBtn").prop("disabled", !conn);

    if (conn) { // 연결 시
        $("#chatTbl").show();
        $("#userTbl").show();
        $("#startBtn").prop("disabled", false);
    }
    else { // 연결 종료 시
        $("#writer").val("");
        $("#msgDiv").css('display', 'none');

        $("#writer").prop("readonly", false);
        $("#writer").css('background-color', 'white');

        $("#userTblBody").html("");

        // 관리자
        $("#noticeDiv").css('display', 'none');
        $("#adminUserTbl").css('display', 'none');

        $("#adminUserTblBody").html("");
    }

    // 연결 or 비연결 누르면 초기화
    $("#chatTblBody").html("");
    $("#adminChatTblBody").html("");
}


// 유저 연결 요청 (유저는 연결 후 writer 입력해야만 채팅, 유저목록 불러와짐)
function connect(userOrAdmin) {

    // SockJS와 stomp.js를 사용해 /ws에 대한 커넥션 생성 (소켓 연결)
    let socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    // connect(headers, connectCallback) 연결되면 아래 익명함수 실행
    stompClient.connect({}, function() {
        console.log('연결 시작');
        connOrDisConn(true);

        if(userOrAdmin == ADMIN) { // 관리자의 경우 연결되자마자 아래 채널들 모두 구독
            $("#adminUserTbl").css('display', 'block');

            stompClient.subscribe('/topic/chat/' + $("#chatRoom_chatRoomName").val(), function(chatMsg) {
                if(JSON.parse(chatMsg.body) != null) {
                    addAdminChatTbl(JSON.parse(chatMsg.body).writer + " : " + JSON.parse(chatMsg.body).msg);
                }
            })

            stompClient.subscribe('/topic/notice/' + + $("#chatRoom_chatRoomName").val(), function(chatMsg){
                if(JSON.parse(chatMsg.body) != null) {
                    addAdminChatTblForNotice(JSON.parse(chatMsg.body).writer + " : " + JSON.parse(chatMsg.body).msg);
                }
            })

            // 유저 목록 받기 위해 admin도 add => send 안하고도 불러올 다른 방법 ??
            stompClient.send("/app/add", {}, JSON.stringify({'writer': 'admin'}));
            subscribeAddUser();

            stompClient.subscribe('/topic/minus', function(users){
                addUserTbl(users.body, ADMIN);
            });

        } else if(userOrAdmin == USER) {
            let writer = $("#chatRoom_chatRoomTempWriter").val();
            stompClient.subscribe("/queue/whisper/" + writer, function(whisper) {

                let whisperBody = JSON.stringify(whisper.body); // JSON => 자바스크립트가 알아듣도록 변경
                let subStrWhisperBody = whisperBody.substring(1, whisperBody.length-1); // 자바에서 String형이기에 넘어온 맨 앞, 맨 뒤 쌍따옴표 없애기

                /* 귓속말에서 발신자 / 수신자 / 귓속말메세지 분리하기 ([/w 수신자 귓속말메세지] 입력하면 컨트롤러에서 [발신자 /w 수신자 귓속말메세지]로 js에게 보냄)*/
                let splitWhisperArr = [];

                if(subStrWhisperBody != null && subStrWhisperBody != '') {
                    for(let i=0; i<subStrWhisperBody.split(" ").length; i++) { // 공백 기준 자르기
                        splitWhisperArr.push(subStrWhisperBody.split(" ")[i]);
                    }
                }

                // 배열 첫번째 인덱스 : 발신자
                let whisperWriter = splitWhisperArr[0];
                console.log('발신인 : ' + whisperWriter);

                // 배열 세번째 인덱스 : 수신자
                let whisperReceiver = splitWhisperArr[2];
                console.log('수신자 : ' + whisperReceiver);

                // 배열 네번째 인덱스 : 귓속말메세지
                let whisperMsg = splitWhisperArr[3];
                console.log('귓속말메세지 : ' + whisperMsg);

                if(whisperReceiver == writer) { // 귓속말 수신자라면
                    addChatTblForWhisper(whisperMsg, whisperWriter, whisperReceiver);
                } else { // 귓속말 발신인이라면
                    addChatMyTblForWhisper(whisperMsg, whisperWriter, whisperReceiver);
                }
             });

//            chatRoom.moveCrossroads(); // 클라이언트에서 서버로 writer 넘겨주기 힘드니까 서버단으로 form 전송해서 값 보내기
/*            checkWriterFrm.submit();*/
        }
    })
}







// 연결 종료 요청
function disconnect() {

    if(stompClient !== null) {
        stompClient.disconnect();
    }

    connOrDisConn(false);
    console.log('연결 종료');
}

function disconnectForTarget() {
    if(stompClient !== null) {
        stompClient.disconnect();
    }

    connOrDisConn(false);
    console.log('연결 종료');
}

/*// writer 입력
function submitWriter() {

    stompClient.send("/app/enter", {}, JSON.stringify({'writer': $('#writer').val(), 'chatRoomName': $("#chatRoom_chatRoomName").val()})); // send(destination, headers = {}, body = '')


*//*    subscribeAddUser();*//*

*//*    // 유저 1명 추가될 때마다 서버에 writer와 chatRoomName send
    stompClient.send("/app/add", {}, JSON.stringify({'writer': $('#writer').val(), 'chatRoomName': $('#chatRoom_chatRoomName').val()}));*//*

    stompClient.subscribe('/topic/chat/' + $("#chatRoom_chatRoomName").val(), function(chatMsg) {
        if(JSON.parse(chatMsg.body).writer == $("#writer").val()){ // writer가 본인이라면
            addChatMyTbl(JSON.parse(chatMsg.body).writer + " : " + JSON.parse(chatMsg.body).msg);
        } else { // writer가 상대라면
            addChatTbl(JSON.parse(chatMsg.body).writer + " : " + JSON.parse(chatMsg.body).msg);
        }
    });

    stompClient.subscribe('/topic/notice/' + $("#chatRoom_chatRoomName").val(), function(chatMsg){
        addChatTblForNotice(JSON.parse(chatMsg.body).writer + " : " + JSON.parse(chatMsg.body).msg);
    });

    stompClient.subscribe('/topic/minus', function(users){
        addUserTbl(users.body, USER);
    });


    // 서버에서 클라이언트별 접속 끊기용으로 구독
    stompClient.subscribe('/queue/disconn/' + $("#writer").val(), function(target) {
        console.log($("#writer").val());
        sendMinusUserFromAdmin($("#writer").val()); // 추후 수정예정
        disconnectForTarget();
    });

}*/
/*
// 유저 추가
function subscribeAddUser() {
    stompClient.subscribe('/topic/add/' + $("#chatRoom_chatRoomName").val(), function(users) {
        addUserTbl(users.body, ADMIN); // 관리자 유저목록에 추가
        addUserTbl(users.body, USER); // 유저의 유저목록에 추가
    });
}*/







// 채팅에 환영인사
function welcome(msg) {
    $("#chatTblBody").append('<tr><td>' + msg + '</td></tr>');
    $("#msgDiv").css('display', 'block');
}


/* 유저 */
// 유저 테이블 추가
function addUserTbl(users, userOrAdmin) {

/*    *//* 자바에서 map을 String 형태로 보내므로 writer(K)와 세션아이디(V)로 잘라내는 과정 필요 *//*
    let userArray = []; // K, V 형태의 배열

    let frontSliceStr = users.slice(2, users.length + 1); // 앞에서부터 특수문자 잘라내기 ('"{')
    let backSliceStr = frontSliceStr.slice(0, frontSliceStr.length - 2); // 뒤에서부터 특수문자 잘라내기 ('}"')

    for(let i=0; i<backSliceStr.split(/":"|","|''/).length; i++) { // 내부 특수문자 잘라내기
        userArray.push(backSliceStr.split(/":"|","|''/)[i]); // 잘라진 것들 유저 목록에 푸시
    }

    // writer 배열
    let writers = [];
    for(let i=0; i<userArray.length; i++) {
        if(i%2 == 0) { // key writer 빼내기
            writers.push(userArray[i]);
        }
    }*/

    // 리스트 형태로 받아온 users [작성자1, 작성자2, 작성자3, ...] 특수문자 잘라내는 과정 필요
    let writerArr = [];

    let sliceSpecialSymbolsStr = users.slice(2, users.length - 2); // 앞에서부터 특수문자 잘라내기 ('"{')

    for(let i=0; i<sliceSpecialSymbolsStr.split('","').length; i++) {
        writerArr.push(sliceSpecialSymbolsStr.split('","')[i]); // "," 로 잘라내어 유저 목록에 푸시
    }

    if(userOrAdmin == USER) {
        /* 사용자 목록 갱신 */
        $("#userTblBody").html(""); // 목록 초기화
        for(let i=0; i<writerArr.length; i++) {
            $("#userTblBody").append('<tr><td>' + writerArr[i] + '</td></tr>'); // 하나씩 달기
        }

    } else if(userOrAdmin == ADMIN) {

        /* 사용자 목록 갱신 */
        $("#adminUserTblBody").html(""); // 목록 초기화
        for(let i=0; i<writerArr.length; i++) { // 테이블 형식으로 추가됨
            $("#adminUserTblBody").append('<tr><td class="adminUserTblTd" value="' + writerArr[i] + '">'
            + writerArr[i]
            + '<button class="btn btn-danger" onclick="sendDisconnect(&quot;' + writerArr[i] + '&quot;)">연결끊기</button>'
            + '</td></tr>'
            );
        }
    }

}

// 관리자의 유저 연결끊기
function sendDisconnect(writer) {
/*    let disconnTarget = writer;*/
    stompClient.send("/app/disconn", {}, JSON.stringify({'writer': writer}));
}

// 관리자의 유저 연결끊기 - 유저목록에서 해당 유저 삭제하기
function sendMinusUserFromAdmin(target) {
    stompClient.send("/app/minus", {}, JSON.stringify({'chatRoomTempWriter': target, 'chatRoomName': $('#chatRoom_chatRoomName').val()}));
}

// 유저 채팅 종료 - 구독
function subscribeMinusUser() {
    stompClient.subscribe('/topic/minus', function(users) {
        addUserTbl(users.body, ADMIN); // 관리자 유저목록에서 제거
        addUserTbl(users.body, USER); // 유저의 유저목록에서 제거
    });
}

// 방장의 특정 유저 강퇴
function sendExpulsionUserFromMaster(target) {
    stompClient.send("/app/expulsion", {}, JSON.stringify({'chatRoomTempWriter': target, 'chatRoomName': $('#chatRoom_chatRoomName').val()}));
}






// 귓속말 전송
function sendMsgForWhisper(receiver) {
    stompClient.send("/app/whisper", {}, JSON.stringify({'writer': $('#chatRoom_chatRoomTempWriter').val(), 'msg': $('#msg').val(), 'receiver': receiver})); // STOMP 통신규약 형식 send(destination, headers = {}, body = '')
}

// 채팅 전송
function sendChat() {
    stompClient.send("/app/chat", {}, JSON.stringify({'writer': $('#chatRoom_chatRoomTempWriter').val(), 'msg': $('#msg').val(), 'chatRoomName': $('#chatRoom_chatRoomName').val()}));
}

// 공지사항 전송
function sendNotice() {
    stompClient.send("/app/notice", {}, JSON.stringify({'writer': 'admin', 'msg': $('#notice').val()}));
}

// 유저 채팅 참여 - 실제 추가는 ajax로, 갱신된 리스트만 가져오기
function getConnWriterListForAddWriter() {
    // 유저 1명 추가될 때마다 서버에 chatRoomName send
    stompClient.send("/app/add", {}, JSON.stringify({'chatRoomName': $('#chatRoom_chatRoomName').val()}));
}

// 유저 채팅 종료 - 실제 삭제는 ajax로, 갱신된 리스트만 가져오기
function getConnWriterListForMinusWriter() {
    stompClient.send("/app/minus", {}, JSON.stringify({'chatRoomName': $('#chatRoom_chatRoomName').val()}));
}





/* 채팅 테이블 추가 */
// 내가 입력한 채팅
function addChatMyTbl(msg) {
    $("#chatTblBody").append('<tr class="trForChatTbl" style="text-align: right; background-color: #FDFD96;"><td>' + msg + '</td></tr>');
}

// 남이 입력한 채팅
function addChatTbl(msg) {
    $("#chatTblBody").append('<tr><td>' + msg + '</td></tr>');
}

// 내가 입력한 귓속말
function addChatMyTblForWhisper(whisperMsg, writer, receiver) {
    $("#chatTblBody").append('<tr class="trForChatTbl" style="text-align: right; background-color: #9EEB47;"><td> [' + writer + ' => ' + receiver +  '] : ' + whisperMsg + '</td></tr>');
}

// 남이 입력한 귓속말
function addChatTblForWhisper(whisperMsg, writer, receiver) {
    $("#chatTblBody").append('<tr style="background-color: #9EEB47;"><td> [' + writer + ' => ' + receiver +  '] : ' + whisperMsg + '</td></tr>');
}

// 유저에게 보이는 공지사항
function addChatTblForNotice(notice) {
    $("#chatTblBody").append('<tr style="text-align: center; background-color: #FFC0CB;"><td>' + notice + '</td></tr>');
}

// 관리자에게 보이는 공지사항
function addAdminChatTblForNotice(notice) {
    $("#adminChatTblBody").append('<tr style="text-align: center; background-color: #FFC0CB;"><td>' + notice + '</td></tr>');
}

// 관리자에게 보이는 남이 입력한 채팅
function addAdminChatTbl(msg) {
    console.log("addAdminChatTbl 호출");
    $("#adminChatTblBody").append('<tr><td class="adminChatTblTd">' + msg + '</td></tr>');
}





// 이벤트
$(function() {
    $("form").click(function(e) {
        e.preventDefault(); // 기본적으로 form 전송 막기
    });


    $("#connectBtn").click(function() {
        connect(USER);
        console.log("connectBtn 클릭");
        $(".checkAttendChat").css('display', 'block');
        $(".attendChatBtn").css('display', 'inline');
        $(".checkAttendChatText").css('display', 'inline');
        $(".exitChatRoomBtn").css('display', 'none');
    });


    $("#disconnectBtn").click(function() {
/*        getConnWriterListForMinusWriter();*/
        if($("#exitChatRoomBtn").attr("display") == 'inline') { // 나가기 클릭된 상태여야만 disconnect 되고, 아니면 alert 창
             disconnect();
             $(".checkAttendChat").css('display', 'none');
        } else {
            alert('나가기 버튼을 먼저 클릭해주세요.');
        }
    });


    $("#startBtn").click(function() {

        // 공백 입력했는지?
        if($("#writer").val().trim() == '') {
            alert("공백을 제외하고 입력해주세요.");
            $("#writer").val("");
            return false;
        }

        // 공백 포함하는지?
        if($("#writer").val().includes(' ')) {
            alert("공백을 제외하고 입력해주세요.");
            $("#writer").val("");
            return false;
        }

        // 길이 초과하는지?
        if($("#writer").val().length > 10) {
            alert("writer는 10글자를 넘을 수 없습니다.");
            $("#writer").val("");
            return false;
        }

        // writer 중복체크
        if(!writer.checkWriter()) { // 중복X
            $("#gettedWriter").val($("#writer").val());
            writerFrmForSubmitCrossroads.submit();
/*
            $("#startBtn").prop("disabled", true);
            $("#writer").prop("readonly", true);
            $("#writer").css('background-color', '#808080');
            $("#chatRoom_chatRoomTempWriter").prop("readonly", true);*/

         }
    });

    $("#sendMsgBtn").click(function() {

        // 공백 제거
        if($("#msg").val().trim() == '') {
            alert("공백은 전송할 수 없습니다.");
            return false;
        }

        // 길이 제한
        if($("#msg").val().length > 100) {
            alert("100자 이상 입력할 수 없습니다.");
            return false;
        }

        // 방장이 강퇴할 경우
        if($("#chatRoom_chatRoomTempWriter").val() == $("#chatRoom_chatRoomMaster").val()) { // 방장일경우
            console.log('방장입니다.');
            if($("#msg").val().substring(0,3) == "/e ") { // /e 강퇴할유저 입력시
                let msg = $("#msg").val();
                let splitMsgArr = [];

                for(let i=0; i<msg.split(" ").length; i++) {
                    splitMsgArr.push(msg.split(" ")[i]);
                    console.log(splitMsgArr[i]);
                }

                let expulsionTarget = splitMsgArr[1]; // 강퇴될 유저

                sendExpulsionUserFromMaster(expulsionTarget); // 강퇴
                $("#msg").val("");
                return false; // 채팅방에 안올려짐
            }
        } else {
            alert("방장이 아니므로 강퇴기능을 사용할 수 없습니다.");
            $("#msg").val("");
            return false; // 채팅방에 안올려짐
        }


        // 귓속말일 경우
        if($("#msg").val().substring(0,3) == "/w ") { // 공백 있어야 귓속말 명령어만 따로 파싱가능

            let msg = $("#msg").val();
            let splitMsgArr = [];

            for(let i=0; i<msg.split(" ").length; i++) {
                splitMsgArr.push(msg.split(" ")[i]);
                console.log(splitMsgArr[i]);
            }


            let receiver = splitMsgArr[1]; // 귓속말 수신자

            if(receiver != $("#writer").val()) { // 귓속말 수신자와 발신자가 다를 때 (즉 본인에게 귓속말 아닐 때만 귓속말 전송됨)
                sendMsgForWhisper(receiver);
            } else {
                alert("본인에게 귓속말 할 수 없습니다.");
            }

            $("#msg").val("/w " + receiver); // receiver에게 귓속말 유지되도록
        } else if($("#msg").val().substring(0,2) == '/q') { // 귓속말 해제한다면
            $("#msg").val("");
        } else { // 일반 채팅이라면
            sendChat();
            $("#msg").val("");
        }
    })


    $("#sendNoticeBtn").click(function() {
        if(!$("#adminConnectBtn").is(":disabled")) {
            alert('웹소켓 연결을 먼저 해주세요.');
            return false;
        }

        if($("#notice").val().trim() == '') {
            alert("공백은 전송할 수 없습니다.");
            return false;
        }

        sendNotice();

        $("#notice").val("");
    });


    $("#noticeBtn").click(function() {
        $("#noticeDiv").css('display', 'block');
    });

    // 관리자 연결
    $("#adminConnectBtn").click(function() {
        connect(ADMIN);
    });

    /* 채팅방 생성 */
    // 정원 줄이기
    $("#downArrow").click(function() {
        let add_chatRoomHeadcountVal = $("#add_chatRoomHeadcount").val();


        if(add_chatRoomHeadcountVal.trim() != '') { // 공백아닐 때
            let add_chatRoomHeadcountValInt = parseInt($("#add_chatRoomHeadcount").val()); // String -> int형 변환

            // 인원 수가 0보다 클 때만 내리기
            if(add_chatRoomHeadcountValInt > 1) {
                add_chatRoomHeadcountValInt = add_chatRoomHeadcountValInt - 1; // 1명씩 감소
                $("#add_chatRoomHeadcount").val(add_chatRoomHeadcountValInt); // val 다시 설정
            } else {
                alert('최소 1명 이상 참여해야 합니다.');
            }
        } else {
            alert('공백을 제외하고 숫자로 입력해주세요.');
        }
    });



    // 정원 늘리기
    $("#upArrow").click(function() {
        let add_chatRoomHeadcountVal = $("#add_chatRoomHeadcount").val();

        if(add_chatRoomHeadcountVal.trim() != '') { // 공백아닐 때
            let add_chatRoomHeadcountValInt = parseInt($("#add_chatRoomHeadcount").val()); // String -> int형 변환
            add_chatRoomHeadcountValInt = add_chatRoomHeadcountValInt + 1; // 1명씩 증가
            $("#add_chatRoomHeadcount").val(add_chatRoomHeadcountValInt); // val 다시 설정
        } else {
            alert('공백을 제외하고 숫자로 입력해주세요.');
        }
    });

    // 채팅방 생성
    $("#addChatRoomBtn").click(function() {
        chatRoom.addChatRoom();
    });

    // 채팅방 생성 버튼 클릭하여 input writer 전송
    $(".moveAddChatRoomBtn").click(function() {
        writerFrmForSubmitChatRoomFrm.submit();
    });

    // 채팅방 목록 버튼 클릭하여 input writer 전송
    $(".moveChatRoomListBtn").click(function() {
        writerFrmForSubmitChatRoomList.submit();
    });


   // 정원 체크 및 입장
    $(".moveSpecificChatRoomBtn").click(function(e) {
        let idx = $(e.target).val(); // class라서 인덱스 구해야 함

        let chatRoomConnWriterCnt = $($(".chatRoomList_chatRoomConnWriterCnt")[idx]).val();
        let chatRoomHeadcount = $($(".chatRoomList_chatRoomHeadcount")[idx]).val();

        // 채팅방 목록에서 채팅방 클릭시 정원 초과하는지 체크
        moveSpecificChatRoom(idx, chatRoomConnWriterCnt, chatRoomHeadcount);
    });

    // 참여 OK 누르면 인원 +1 명 증가
    $(".attendChatBtn").click(function() {
        if($("#connectBtn").attr("disabled")) { // 연결버튼을 먼저 클릭해둔 상태라면
            /* 연결 성공하면 인사 날리기 */
            // subscribe(destination, callback, headers = {})
            // subscribe(이벤트[라우팅], 실행할 함수)
            stompClient.subscribe('/topic/enter/' + $("#chatRoom_chatRoomName").val(), function(ChatEnter){
                // welcomeMsg : 커맨드객체(chatEnter)의 필드명의 값(welcomeMsg) 가져옴
    /*            welcome(JSON.parse(chatEnter.body).welcomeMsg); // JSON 데이터로 변환후 content만 빼내서 테이블에 환영인사 날림*/
                welcome(JSON.parse(ChatEnter.body).welcomeMsg);
            });

            stompClient.send("/app/enter", {}, JSON.stringify({'writer': $('#chatRoom_chatRoomTempWriter').val(), 'chatRoomName': $("#chatRoom_chatRoomName").val()})); // send(destination, headers = {}, body = '')


        /*    subscribeAddUser();*/

            // 유저 1명 추가될 때마다 서버에 writer와 chatRoomName send
            stompClient.send("/app/add", {}, JSON.stringify({'chatRoomTempWriter': $('#chatRoom_chatRoomTempWriter').val(), 'chatRoomName': $('#chatRoom_chatRoomName').val()}));

            stompClient.subscribe('/topic/chat/' + $("#chatRoom_chatRoomName").val(), function(chatMsg) {
                if(JSON.parse(chatMsg.body).writer == $("#chatRoom_chatRoomTempWriter").val()){ // writer가 본인이라면
                    addChatMyTbl(JSON.parse(chatMsg.body).writer + " : " + JSON.parse(chatMsg.body).msg);
                } else { // writer가 상대라면
                    addChatTbl(JSON.parse(chatMsg.body).writer + " : " + JSON.parse(chatMsg.body).msg);
                }
            });

            stompClient.subscribe('/topic/notice/' + $("#chatRoom_chatRoomTempWriter").val(), function(chatMsg){
                addChatTblForNotice(JSON.parse(chatMsg.body).writer + " : " + JSON.parse(chatMsg.body).msg);
            });

            // 방장이 특정 유저 강퇴하기 위해 구독
            stompClient.subscribe('/queue/expulsion/' + $("#chatRoom_chatRoomTempWriter").val(), function(users) {

                // 나가기 및 DISCONN 과 동일한 효과
/*                chatRoom.minusWriterCntForChatRoom();*/
                disconnect();

                // 이동
                writerFrmForSubmitChatRoomList.submit();
            });


            // 서버에서 클라이언트별 접속 끊기용으로 구독
            stompClient.subscribe('/queue/disconn/' + $("#chatRoom_chatRoomTempWriter").val(), function(target) {
                console.log($("#chatRoom_chatRoomTempWriter").val());
                sendMinusUserFromAdmin($("#chatRoom_chatRoomTempWriter").val());
                disconnectForTarget();
            });

            stompClient.subscribe('/topic/add/' + $("#chatRoom_chatRoomName").val(), function(users) {
                addUserTbl(users.body, ADMIN); // 관리자 유저목록에 추가
                addUserTbl(users.body, USER); // 유저의 유저목록에 추가
            });

            stompClient.subscribe('/topic/minus/' + $("#chatRoom_chatRoomName").val(), function(users){
                addUserTbl(users.body, ADMIN);
                addUserTbl(users.body, USER);
            });

            chatRoom.addWriterCntForChatRoom(); // +1 됨

            $(".attendChatBtn").css('display', 'none');
            $(".checkAttendChatText").css('display', 'none');
            $(".exitChatRoomBtn").css('display', 'inline');
        } else {
            alert("CONNECT 버튼을 클릭하여 먼저 연결을 시도해주세요.");
        }
    });

    // 나가기 누르면 인원 -1 명 증가 and chatRoomList로 이동
    $(".exitChatRoomBtn").click(function() {
        chatRoom.minusWriterCntForChatRoom();
        disconnect();
        $(".exitChatRoomBtn").css('display', 'none');
        $(".attendChatBtn").css('display', 'inline');
        $(".checkAttendChatText").css('display', 'inline');
        // writerFrmForSubmitChatRoomList.submit(); // input writer 전송됨
    });

    $("#voteCreateModalOpenBtn").click(function() {
        $("#voteCreateDiv").css('display', 'flex');
    })

    $("#voteCreateModalCloseDiv").click(function() {
        $("#voteCreateDiv").css('display', 'none');
    })



});

// !! 목록 새로고침 안하는 이상 실시간 갱신 안되어서
// 정원 초과된 상태라도 동시접속 가능할 수도 있음. 이 부분 고민해보기
// DB 사용하는게 아니라서 갱신 안되려나?
function moveSpecificChatRoom(idx, chatRoomConnWriterCnt, chatRoomHeadcount) {

    if(chatRoomConnWriterCnt == chatRoomHeadcount) { // 정원 꽉 차있다면 입장 불가능
        alert('정원 초과하여 입장 불가능합니다.');
    } else { // 정원 꽉 안차있다면 입장 가능
        $(".chatRoomInfoFrm")[idx].submit();
    }

}



/* Ajax */
let writer = {
    init : function() {

    },

    // Writer 중복체크
    checkWriter: function() {
        let flagForCheckUser; // Writer 중복체크 불리언 여부 // false면 중복X true면 중복O
        let inputWriterForCheckDup = $("#writer").val();

        // 공백 조건
        if(inputWriterForCheckDup.trim() == ''){
            alert('공백을 제외하고 입력해주세요.');
            return false;
        }

        let data = {
            inputWriterForCheckDup: inputWriterForCheckDup
        }

        $.ajax({
            url: "/checkWriter",
            async: false, // 값을 리턴시 해당코드를 추가하여 동기로 변경 (return 값이 undefiend 되는 것을 막음)
            type: "POST",
            data: JSON.stringify(data),
            contentType: "application/json; charset=utf-8",
            dataType: "json"
        }).done(function(resp) {
            if(!resp) {
                alert('사용 가능한 Writer입니다.');
                flagForCheckUser = false;
            } else {
                alert('중복된 Writer입니다.\n다른 Writer를 입력해주세요!');
                flagForCheckUser = true;
                $("#writer").val("");
            }
        }).fail(function(error) {
            alert(JSON.stringify(error));
        });
        return flagForCheckUser;
    },

}

let chatRoom = {
    init : function() {

    },


    // 채팅방 생성
    addChatRoom: function() {
        let add_chatRoomMaster = $("#add_chatRoomMaster").val();
        let add_chatRoomName = $("#add_chatRoomName").val();
        let add_chatRoomHeadcount = $("#add_chatRoomHeadcount").val();


        // 공백 조건
        if(add_chatRoomName == ''){
            alert('공백 제외하고 입력해주세요.');
            return false;
        }

        if(add_chatRoomHeadcount == ''){
            alert('공백 제외하고 입력해주세요.');
            return false;
        }

        let data = {
            chatRoomMaster: add_chatRoomMaster,
            chatRoomName: add_chatRoomName,
            chatRoomHeadcount: add_chatRoomHeadcount
        }

        $.ajax({
            url: "/chatRoom",
            type: "POST",
            data: JSON.stringify(data),
            contentType: "application/json; charset=utf-8",
            dataType: "json"
        }).done(function(resp) {
            if(resp) { // 정상 리턴한다면 (커맨드 객체 리턴)
                writerFrmForSubmitChatRoom.action = "/chatRoom/" + resp.chatRoomName;
                writerFrmForSubmitChatRoom.submit();
            }
        }).fail(function(error) {
            alert(JSON.stringify(error));
        });
    },


    // 특정 채팅방 접속 후 Writer 입력하면 채팅방 인원 +1 증가
    addWriterCntForChatRoom: function() {
        let data = {
            chatRoomTempWriter: $("#chatRoom_chatRoomTempWriter").val(),
            chatRoomName: $("#chatRoom_chatRoomName").val()
        }

        $.ajax({
            url: "/chatRoom/plusConnWriterCnt",
            type: "PUT",
            data: JSON.stringify(data),
            contentType: "application/json; charset=utf-8",
            dataType: "json"
        }).done(function(resp) {
            console.log("resp : " + resp);

            if(!resp) { // false (정원 추가 안됨)
                alert('정원을 초과하여 입장할 수 없습니다.');
            } else { // true (정원 추가됨)
                // alert('채팅에 입장합니다.'); // 인사말 보내는걸로 변경하기
            }

           getConnWriterListForAddWriter();
        }).fail(function(error) {
            alert(JSON.stringify(error));
        });
    },



    // 특정 채팅방 접속 후 Writer 입력하면 채팅방 인원 -1 감소
    minusWriterCntForChatRoom: function() {
        let data = {
            chatRoomTempWriter: $("#chatRoom_chatRoomTempWriter").val(),
            chatRoomName: $("#chatRoom_chatRoomName").val()
        }

        $.ajax({
            url: "/chatRoom/minusConnWriterCnt",
            type: "PUT",
            data: JSON.stringify(data),
            contentType: "application/json; charset=utf-8",
            dataType: "json"
        }).done(function(resp) {
            if(resp) {
                getConnWriterListForMinusWriter();
            }
        }).fail(function(error) {
            alert(JSON.stringify(error));
        });
    },


}

// 호출
writer.init();
chatRoom.init();
