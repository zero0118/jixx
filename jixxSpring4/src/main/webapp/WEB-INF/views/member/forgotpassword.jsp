<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<jsp:include page="../template/header.jsp" flush="false" />
<script type="text/javascript">
	function getXMLHttpRequest() {
		if (window.ActiveXObject) {
			try {
				return new ActiveXObject("Msxml2.XMLHTTP");
			} catch (e) {
				try {
					return new ActiveXObject("Microsoft.XMLHTTP");
				} catch (e1) {
					return null;
				}
			}
		} else if (window.XMLHttpRequest) {
			return new XMLHttpRequest();
		} else {
			return null;
		}
	}
	var httpRequest = null;

	function sendRequest(url, params, callback, method) {
		httpRequest = getXMLHttpRequest();
		var httpMethod = method ? method : 'GET';
		if (httpMethod != 'GET' && httpMethod != 'POST') {
			httpMethod = 'GET';
		}
		var httpParams = (params == null || params == '') ? null : params;
		var httpUrl = url;
		if (httpMethod == 'GET' && httpParams != null) {
			httpUrl = httpUrl + "?" + httpParams;
		}
		httpRequest.open(httpMethod, httpUrl, true);
		httpRequest.setRequestHeader(
			'Content-Type', 'application/x-www-form-urlencoded');
		httpRequest.onreadystatechange = callback;
		httpRequest.send(httpMethod == 'POST' ? httpParams : null);
	}

	function check() {
		var email = $('#emailinput').val();
		if (email == "") {
			alert("아이디를입력하세요");
		} else {
			if (email.length <= 10) {
				alert("전체아이디를입력하세요")
			} else {
				sendRequest("<%=request.getContextPath()%>/idCheck.do?email=" + email + "&checkfrom=forgotpass", null, check_res1, "GET");

			}
		}
	}
	function check_res1() {
		if (httpRequest.readyState == 4) {
			if (httpRequest.status = 200) {
				var check_div = document.getElementById("check_div");
				check_div.innerHTML = httpRequest.responseText;
			}
		}
	}
	$(function() {
		$('#verify').click(function() {
			var idCheckResult = $('#check_div').text().trim();
			if (idCheckResult == "") {
				alert("아이디 체크 먼저 하세요")
			} else {
				if (idCheckResult == "존재하지 않는 아이디입니다 회원가입하세요") {
					alert("로그인창으로이동합니다")
					$(location).attr('href', "${pageContext.request.contextPath}/member/signup.do");
				} else {
					$('#insertform').submit();
				}
			}
		})
	})
</script>
		<div class="content-wrapper" style="min-height: 863px;">
			<div class="container">
				<!-- Main content -->
				<section class="content">
					<div class="col-lg-16">
						<div class="card">
							<div class="card-body card-block">
								<h3>Find forgotten password</h3>
								<br> <label class="control-label mb-1">Your email
									address</label>
								<form action="${pageContext.request.contextPath }/emailauth.do"
									method="post" class="form-horizontal" id="insertform">
									<div class="row form-group">
										<div class="col-6">
											<input type="email" name="email" class="form-control"
												id="emailinput"> <input type="hidden"
												name="requestfrom" value="findpass">
										</div>
									</div>
									<div>
										<input type="button" value="아이디체크" onclick="check()"
											class="btn btn-primary btn-block btn-flat">
									</div>
									<div id="check_div"></div>
									<button type="button" class="btn btn-block btn-primary btn-lg"
										id="verify">인증번호전송</button>
								</form>
								<br>
							</div>
						</div>
					</div>
				</section>
			</div>
		</div>
</body>