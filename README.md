# 미친네컷 :

**당신만의 프레임을 만들고, 사진을 찍을 수 있는 추억 저장소**

![Group 19](https://github.com/user-attachments/assets/b09fd2aa-62a2-48e4-a2fb-3875cfd10897)


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

<img width="1035" alt="Untitled" src="https://github.com/user-attachments/assets/bc1720c0-9565-4982-8160-fc72de18a5c4" />


https://github.com/user-attachments/assets/15071bf0-1730-4543-a3a3-dccfd6bb3a6f



## 구현 기술 소개

### 로그인

- 클라이언트는 네이버 로그인 API를 이용합니다.
- 네이버 로그인을 성공하여 받은 토큰은 서버에게 전달됩니다.
- 서버는 해당 토큰을 파싱하여 네이버 ID, 이름을 얻고 `MemberRepository`에 해당 네이버 ID가 없다면, `Member`로 저장합니다.
- 서버는 클라이언트의 로그인을 위해 해당 `memberId`와 `JWT`를 이용하여 서버 토큰을 발급합니다 이는 로그인의 `Response`로 전송합니다.
- 클라이언트는 서버 토큰을 `SharedPreferences`에 저장하여, 토큰을 손쉽게 관리합니다.
    
![Untitled](https://github.com/user-attachments/assets/7ce7c7fa-4839-410c-a6d5-4a18bce965c8)

    
- 해당 `userId`는 현재 네이버에 의해 암호화된 아이디입니다.
- 서버는 `Request`가 오면, `Header`에서 클라이언트가 보낸 서버 토큰을 가져와 파싱을 하여 `memberId`를 얻고, 해당 `Member`를 특정할 수 있습니다.
    
<img width="363" alt="Untitled" src="https://github.com/user-attachments/assets/3d846fe2-3c58-4177-a382-ca9d562f1d2e" />

    
- **결과적으로 클라이언트는 서버에게 받은 토큰을 `Request Header`에 넣어 저희 서비스를 이용할 수 있게 됩니다.**
    
![Untitled](https://github.com/user-attachments/assets/1bc26e5e-e599-4057-9527-340eb9d1d3e1)

    

### Gallery

- 클라이언트는 개인의 갤러리를 갖습니다.
- 이 갤러리는 네컷이 들어간 이미지를 저장하는 공간입니다.
- 갤러리 탭으로 이동 시, 프론트엔드와 백엔드의 HTTP API 통신을 합니다.
- 서버는 해당 `memberId`의 `Image Entity` 객체 `List`를 반환합니다.
    
<img width="523" alt="Untitled" src="https://github.com/user-attachments/assets/afaf1e0f-11b1-4994-be87-bcfd6ce3fb35" />

    
![Untitled](https://github.com/user-attachments/assets/faccb1cf-f58c-4518-9c0d-6ab38dc9e182)


    
- 위 사진은 현재 `memberId`가 2밖에 없지만, 다른 `memberId`도 생긴다면, 정확히 개인의 갤러리를 가질 수 있습니다.
- 로드된 사진을 클릭하면 DIAGLOG가 나타나며, 사진을 더 크게 볼 수 있고, 갤러리에 다운로드할 수 있습니다.
- 다운로드를 한다면, `toast Message`가 나타나며, 기기 내 갤러리에 저장됩니다.
- 데이터베이스 이미지 저장 방식
    - 로컬 파일 시스템에 실제 사진 파일을 저장하며, 데이터베이스에는 해당 사진의 `url`만 저장합니다.
    - 이는 데이터베이스의 간결함을 유지시켜줍니다.
        
![Untitled](https://github.com/user-attachments/assets/6df03391-bc77-4126-8dbf-a9e496c9c021)



https://github.com/user-attachments/assets/85817305-c436-4e81-82f3-79a603636d46



### Sticker

- 해당 탭으로 이동 시, 서버는 이미지와 똑같이 통신을 통해 `Member`만의 `StickerList`를 반환합니다.
- 클라이언트는 `Sticker` 탭에서 자신만의 스티커를 관리할 수 있습니다.
    
![Untitled](https://github.com/user-attachments/assets/f0976cd3-9fba-459b-9733-5762d04d9015)

- 클라이언트는 `Upload` 버튼을 통해 사진을 서버에 전송합니다.
- 서버는 전송받은 사진 파일을 Rmovebg API를 이용하여 사진의 백그라운드를 제거한 후, `StickerRepository`에 저장됩니다.
    
<img width="729" alt="Untitled" src="https://github.com/user-attachments/assets/77ec5c43-1f80-41ea-8af0-48e642c46b7c" />



https://github.com/user-attachments/assets/276111dc-096e-4a3b-8b7d-34ec24c351c4

    

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

![Screenshot_20240710-194321](https://github.com/user-attachments/assets/5011480b-25c4-44d6-a84c-1653c04e71d8) ![Screenshot_20240710-194330](https://github.com/user-attachments/assets/68770443-6b37-4ff0-aeda-cd5274a28104) ![Screenshot_20240710-194339](https://github.com/user-attachments/assets/a4e03c48-e328-431c-bca3-f879b0290cb5)





## Beta version APK link

https://drive.google.com/file/d/11LFPV_AwnxTneHpBMK54PnoC7hobNNGk/view?usp=sharing
