# EmoTracker

Tracking emotion is a way to monitor and measure our mental state. This will help us to understand how we feel and could be a huge help to improve our mental healthiness. EmoTracker is an app to help you to track your emotions on a daily basis easily. To track your emotions, you can just simply capture your face and let the app recognize your emotion! The app is powered by machine learning to recognize emotion from our face automatically.

## Features

✅Recognize 7 emotions (anger 😡, disgust 🤢, fear 😨, happiness 🙂, neutral 😐, sadness 😢, and surprise 😨) from face images

✅Record and track emotions on a daily basis

## Model Training

We develop our emotion recognition model using 13,690 face images from [facial_expression](https://github.com/muxspace/facial_expressions). The summary of the dataset are as follows.
|Emotion category|Total |
|----------------|-----:|
|anger           |252   |
|contempt        |9     |
|disgust         |208   |
|fear            |21    |
|happiness       |5,696 |
|neutral         |6,868 |
|sadness         |268   |
|surprise        |368   |

We remove contempt from our training data due to lack of data. Then, MobileNet is employed to train our model with fine-tuning. Finally, the model is converted to TFLite to be used in the Android app.

We believe there are a lot of room for improvement to develop the model:
- Handle the imbalanced data
- Hyperparameter tuning
- Re-train the model using other algorithm
- Use higher quality dataset

To understand further about how we train and evaluate the model, you can see it here.
