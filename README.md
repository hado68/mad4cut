# 미친네컷 :

**당신만의 프레임을 만들고, 사진을 찍을 수 있는 추억 저장소**

![Group 19.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/2b6badba-99a4-40c4-8271-59489b632849/Group_19.png)

# 팀원 소개

김진영

- 전남대학교 19학번
- 백엔드

하도현

- 카이스트 22학번
- 프론트엔드

# 개발 스택 소개

- **프론트엔드:** AndroidStudio (언어: kotlin)
- **백엔드-서버:** SpringBoot, Spring JPA
- **Cloud:** AWS
- **DB:** Docker
- **SDK:** 네이버
- **디자인:** Figma

## API 명세서

![Untitled](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/86706ccd-4a35-467c-84b9-46e53dbc8bba/Untitled.png)

[스플래시 ](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/9e63da46-db4b-4a3f-a8ed-2de74623c15f/XRecorder_10072024_193548.mp4)

스플래시 

## 구현 기술 소개

### 로그인

- 클라이언트는 네이버 로그인 API를 이용합니다.
- 네이버 로그인을 성공하여 받은 토큰은 서버에게 전달됩니다.
- 서버는 해당 토큰을 파싱하여 네이버 ID, 이름을 얻고 `MemberRepository`에 해당 네이버 ID가 없다면, `Member`로 저장합니다.
- 서버는 클라이언트의 로그인을 위해 해당 `memberId`와 `JWT`를 이용하여 서버 토큰을 발급합니다 이는 로그인의 `Response`로 전송합니다.
- 클라이언트는 서버 토큰을 `SharedPreferences`에 저장하여, 토큰을 손쉽게 관리합니다.
    
    ![Untitled](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/e910c4b7-196e-49d5-a3ef-91c9c7f7d2c0/Untitled.png)
    
- 해당 `userId`는 현재 네이버에 의해 암호화된 아이디입니다.
- 서버는 `Request`가 오면, `Header`에서 클라이언트가 보낸 서버 토큰을 가져와 파싱을 하여 `memberId`를 얻고, 해당 `Member`를 특정할 수 있습니다.
    
    ![Untitled](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/c138a2ca-4207-42e6-947a-123215b7cfee/Untitled.png)
    
- **결과적으로 클라이언트는 서버에게 받은 토큰을 `Request Header`에 넣어 저희 서비스를 이용할 수 있게 됩니다.**
    
    ![Untitled](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/b81fe547-b204-41da-9956-ab23956ffcc7/Untitled.png)
    

### Gallery

- 클라이언트는 개인의 갤러리를 갖습니다.
- 이 갤러리는 네컷이 들어간 이미지를 저장하는 공간입니다.
- 갤러리 탭으로 이동 시, 프론트엔드와 백엔드의 HTTP API 통신을 합니다.
- 서버는 해당 `memberId`의 `Image Entity` 객체 `List`를 반환합니다.
    
    ![Untitled](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/fbb7f8c6-0bf9-4026-8f41-89439ccf4479/Untitled.png)
    
    ![Untitled](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/817d774f-8280-43b6-9a05-6bd064f794f9/Untitled.png)
    
- 위 사진은 현재 `memberId`가 2밖에 없지만, 다른 `memberId`도 생긴다면, 정확히 개인의 갤러리를 가질 수 있습니다.
- 로드된 사진을 클릭하면 DIAGLOG가 나타나며, 사진을 더 크게 볼 수 있고, 갤러리에 다운로드할 수 있습니다.
- 다운로드를 한다면, `toast Message`가 나타나며, 기기 내 갤러리에 저장됩니다.
- 데이터베이스 이미지 저장 방식
    - 로컬 파일 시스템에 실제 사진 파일을 저장하며, 데이터베이스에는 해당 사진의 `url`만 저장합니다.
    - 이는 데이터베이스의 간결함을 유지시켜줍니다.
        
        ![Untitled](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/1b654b23-d88d-45a0-b1a0-be78c8bee050/Untitled.png)
        

[갤러리 뷰](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/81b79a7a-eb59-4f7a-a3d2-82c9e060c171/XRecorder_10072024_201934.mp4)

갤러리 뷰

### Sticker

- 해당 탭으로 이동 시, 서버는 이미지와 똑같이 통신을 통해 `Member`만의 `StickerList`를 반환합니다.
- 클라이언트는 `Sticker` 탭에서 자신만의 스티커를 관리할 수 있습니다.
    
    ![Untitled](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/b40d5ab3-d9bb-4ac5-bf01-2cf9e8b76579/Untitled.png)
    
- 클라이언트는 `Upload` 버튼을 통해 사진을 서버에 전송합니다.
- 서버는 전송받은 사진 파일을 Rmovebg API를 이용하여 사진의 백그라운드를 제거한 후, `StickerRepository`에 저장됩니다.
    
    ![Untitled](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/abbc5f2c-681f-4676-8611-07751a28c66d/Untitled.png)
    
    [스티커 ](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/8108de65-e4cb-4610-87f4-4ddbe1b5e19f/XRecorder_10072024_200832.mp4)
    
    스티커 
    

### Home

- 클라이언트는 `Home` 탭에서 카메라를 이용해 사진을 찍을 수 있습니다.
- `TextureView`를 이용해 카메라 세션이 애플리케이션 내에서 열릴 수 있도록 합니다.
- 총 4장의 사진을 찍을 수 있고, 이 사진들을 이용해 한 장의 사진으로 구성합니다.
- 클라이언트는 카메라 세션이 종료된 이후, 사진의 프레임을 정할 수 있습니다.
- 프레임을 정한 이후, 다음으로 버튼을 클릭시 스티커를 이용해 사진을 꾸밀 수 있습니다.
    - 해당 스티커는 다음의 기능을 같습니다.
        - 스티커를 드래그하여 이동 기능
        - 스티커 크기 키우기 및 줄이기 기능
        - 스티커 `recyclerView`를 토글버튼을 이용하여 숨기기 및 펼치기 기능
        - 이후 저장하기 버튼을 누른다면 첫번째에 나왔던 `ImageRepository`에 저장됩니다.

[네컷 ](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/7f0fa1ff-af8d-4c1c-8159-2aa8f4c23368/XRecorder_10072024_194037.mp4)

네컷 

![Screenshot_20240710-194321.jpg](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/d5434fd2-b40b-4001-857a-86cf2fa4da79/Screenshot_20240710-194321.jpg)

![Screenshot_20240710-194330.jpg](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/83c45556-5f51-4a7f-bea8-f23df3e558fc/Screenshot_20240710-194330.jpg)

![Screenshot_20240710-194339.jpg](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/fb8ea749-78e5-4d03-bd78-8f05d5bcffaf/Screenshot_20240710-194339.jpg)

![Screenshot_20240710-194353.jpg](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/cb6bcb06-be71-4d49-afcf-73ba532680ee/Screenshot_20240710-194353.jpg)

![Screenshot_20240710-194349.jpg](https://prod-files-secure.s3.us-west-2.amazonaws.com/f6cb388f-3934-47d6-9928-26d2e10eb0fc/074cf2fe-4a53-4afb-bc2f-d300ae3c42ad/Screenshot_20240710-194349.jpg)

## Beta version APK link

https://drive.google.com/file/d/11LFPV_AwnxTneHpBMK54PnoC7hobNNGk/view?usp=sharing
